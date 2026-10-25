package net.logicsquad.minifier;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;

import net.logicsquad.minifier.css.CSSMinifier;
import net.logicsquad.minifier.js.JSMinifier;

/**
 * Goal for minification of web resources.
 *
 * @author paulh
 */
@Mojo(name = "minify", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MinifierMojo extends AbstractMojo {
	/**
	 * Source directory
	 */
	@Parameter(property = "sourceDir", required = true)
	private File sourceDir;

	/**
	 * Target directory
	 */
	@Parameter(property = "targetDir", required = true)
	private File targetDir;

	/**
	 * List of Javascript includes
	 */
	@Parameter(property = "jsIncludes")
	private List<String> jsIncludes;

	/**
	 * List of Javascript excludes
	 */
	@Parameter(property = "jsExcludes")
	private List<String> jsExcludes;

	/**
	 * List of CSS includes
	 */
	@Parameter(property = "cssIncludes")
	private List<String> cssIncludes;

	/**
	 * List of CSS excludes
	 */
	@Parameter(property = "cssExcludes")
	private List<String> cssExcludes;

	/**
	 * Whether to skip minification
	 */
	@Parameter(property = "minifier.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Encoding of resources that don't start with a byte order mark, to read and
	 * write them in
	 */
	@Parameter(property = "minifier.encoding", defaultValue = "UTF-8")
	private String encoding;

	/**
	 * Resolved list of Javascript filenames to minify
	 */
	private List<String> jsFilenames;

	/**
	 * Resolved list of CSS filenames to minify
	 */
	private List<String> cssFilenames;

	/**
	 * Returns resolved list of Javascript filenames for minification.
	 *
	 * @return Javascript filenames
	 */
	private List<String> jsFilenames() {
		if (jsFilenames == null) {
			jsFilenames = filenameList(jsIncludes, jsExcludes);
		}
		return jsFilenames;
	}

	/**
	 * Returns resolved list of CSS filenames for minification.
	 *
	 * @return CSS filenames
	 */
	private List<String> cssFilenames() {
		if (cssFilenames == null) {
			cssFilenames = filenameList(cssIncludes, cssExcludes);
		}
		return cssFilenames;
	}

	/**
	 * Returns filename list from includes and excludes.
	 *
	 * @param includes list of include patterns
	 * @param excludes list of exclude patterns
	 * @return filename list
	 */
	private List<String> filenameList(List<String> includes, List<String> excludes) {
		List<String> list = new ArrayList<>();
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(sourceDir);
		scanner.setIncludes(includes.toArray(new String[0]));
		scanner.setExcludes(excludes.toArray(new String[0]));
		scanner.addDefaultExcludes();
		scanner.scan();
		for (String s : scanner.getIncludedFiles()) {
			list.add(s);
		}
		return list;
	}

	/**
	 * Performs Javascript and CSS minification.
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping minification");
			return;
		}
		if (!sourceDir.isDirectory()) {
			throw new MojoFailureException("sourceDir is not an existing directory: '" + sourceDir + "'");
		}
		Charset charset;
		try {
			charset = Charset.forName(encoding);
		} catch (IllegalArgumentException e) {
			throw new MojoFailureException("encoding is not a supported encoding: '" + encoding + "'");
		}
		minify(new JSMinifier(), jsFilenames(), charset);
		minify(new CSSMinifier(), cssFilenames(), charset);
	}

	/**
	 * Minifies {@link File}s represented by {@code filenames} using {@code minifier}.
	 *
	 * @param minifier  minifier for the resources
	 * @param filenames list of filenames
	 * @param charset   encoding of resources that don't start with a byte order mark
	 * @throws MojoFailureException   if a resource isn't valid in its encoding, or
	 *                                can't be minified
	 * @throws MojoExecutionException if a resource can't be read or written, or
	 *                                minifying it fails unexpectedly
	 */
	private void minify(Minifier minifier, List<String> filenames, Charset charset)
			throws MojoExecutionException, MojoFailureException {
		for (String s : filenames) {
			try {
				File infile = new File(sourceDir, s);
				File outfile = new File(targetDir, s);
				byte[] input = Files.readAllBytes(infile.toPath());
				Charset inputCharset = encodingOf(input, charset);
				// Each character in the output comes from the input, or is ASCII, so it can be
				// encoded.
				byte[] output = minifier.minify(decode(input, inputCharset)).getBytes(inputCharset);
				// Nothing is written until minification succeeds, so a failure leaves the
				// output file as it was, which is the input when targetDir is sourceDir.
				// Depending on where or how the plugin is invoked, the parent directories above
				// the output file may not exist yet.
				Files.createDirectories(outfile.toPath().getParent());
				Files.write(outfile.toPath(), output);
				logMinificationResult(s, input.length, output.length);
			} catch (MinificationException e) {
				throw new MojoFailureException("Unable to minify '" + s + "': " + e.getMessage(), e);
			} catch (IOException | RuntimeException e) {
				throw new MojoExecutionException("Unable to minify '" + s + "': " + e, e);
			}
		}
	}

	/**
	 * Returns {@code input} decoded from {@code charset}. Unlike
	 * {@link String#String(byte[], Charset)}, which replaces each byte sequence that
	 * isn't valid with U+FFFD, this fails, and says where.
	 *
	 * @param input   bytes of a resource
	 * @param charset encoding of the resource
	 * @return the resource's text
	 * @throws MinificationException if {@code input} isn't valid in {@code charset}
	 */
	private static String decode(byte[] input, Charset charset) throws MinificationException {
		ByteBuffer bytes = ByteBuffer.wrap(input);
		try {
			return charset.newDecoder().decode(bytes).toString();
		} catch (CharacterCodingException e) {
			// The decoder stops at the start of the bytes that aren't valid, so everything
			// before them decodes, to the text in front of the problem.
			String before = new String(input, 0, bytes.position(), charset);
			throw new MinificationException("Not valid " + charset.name(), before, before.length());
		}
	}

	/**
	 * Returns the encoding given by the byte order mark that {@code input} starts
	 * with, or {@code charset} if it doesn't start with one. As in browsers, a byte
	 * order mark takes precedence over any other indication of the encoding. The
	 * encodings returned for one keep it as U+FEFF, so it's written out again.
	 *
	 * @param input   bytes of a resource
	 * @param charset encoding of resources that don't start with a byte order mark
	 * @return encoding of the resource
	 */
	private static Charset encodingOf(byte[] input, Charset charset) {
		if (startsWith(input, 0xEF, 0xBB, 0xBF)) {
			return StandardCharsets.UTF_8;
		}
		if (startsWith(input, 0xFE, 0xFF)) {
			return StandardCharsets.UTF_16BE;
		}
		if (startsWith(input, 0xFF, 0xFE)) {
			return StandardCharsets.UTF_16LE;
		}
		return charset;
	}

	/**
	 * Returns whether {@code bytes} starts with {@code prefix}.
	 *
	 * @param bytes  bytes to look at
	 * @param prefix bytes to look for, each from 0 to 255
	 * @return whether {@code bytes} starts with {@code prefix}
	 */
	private static boolean startsWith(byte[] bytes, int... prefix) {
		if (bytes.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if ((bytes[i] & 0xFF) != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Logs minification result.
	 *
	 * @param name filename
	 * @param pre  input size in bytes
	 * @param post output size in bytes
	 */
	private void logMinificationResult(String name, long pre, long post) {
		// A percentage reduction means nothing for an empty resource.
		if (pre == 0) {
			getLog().info("Minified '" + name + "' " + pre + " -> " + post);
			return;
		}
		long reduction = (long) (100.0 - (((double) post / (double) pre) * 100.0));
		getLog().info("Minified '" + name + "' " + pre + " -> " + post + " (" + reduction + "%)");
		return;
	}
}