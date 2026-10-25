package net.logicsquad.minifier;

import java.io.File;
import java.io.IOException;
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
		if (!sourceDir.isDirectory()) {
			throw new MojoFailureException("sourceDir is not an existing directory: '" + sourceDir + "'");
		}
		minify(new JSMinifier(), jsFilenames());
		minify(new CSSMinifier(), cssFilenames());
	}

	/**
	 * Minifies {@link File}s represented by {@code filenames} using {@code minifier}.
	 *
	 * @param minifier  minifier for the resources
	 * @param filenames list of filenames
	 * @throws MojoFailureException   if a resource can't be minified
	 * @throws MojoExecutionException if a resource can't be read or written, or
	 *                                minifying it fails unexpectedly
	 */
	private void minify(Minifier minifier, List<String> filenames) throws MojoExecutionException, MojoFailureException {
		for (String s : filenames) {
			try {
				File infile = new File(sourceDir, s);
				File outfile = new File(targetDir, s);
				byte[] input = Files.readAllBytes(infile.toPath());
				byte[] output = minifier.minify(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
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