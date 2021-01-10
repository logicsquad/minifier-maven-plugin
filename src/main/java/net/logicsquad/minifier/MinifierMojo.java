package net.logicsquad.minifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
	private String sourceDir;

	/**
	 * Target directory
	 */
	@Parameter(property = "targetDir", required = true)
	private String targetDir;

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
		minify(JSMinifier.class, jsFilenames());
		minify(CSSMinifier.class, cssFilenames());
		return;
	}

	/**
	 * Minifies {@link File}s represented by {@code filenames} using an instance of
	 * {@code minifierClass}.
	 *
	 * @param minifierClass class implementing {@link Minifier}
	 * @param filenames     list of filenames
	 * @throws MojoFailureException if any exception is caught during minification
	 */
	private void minify(Class<? extends Minifier> minifierClass, List<String> filenames) throws MojoFailureException {
		for (String s : filenames) {
			try {
				File infile = new File(sourceDir, s);
				File outfile = new File(targetDir, s);
				Constructor<? extends Minifier> constructor = minifierClass.getConstructor(Reader.class);
				Minifier minifier = constructor.newInstance(
						new InputStreamReader(Files.newInputStream(infile.toPath()), StandardCharsets.UTF_8));
				minifier.minify(
						new OutputStreamWriter(Files.newOutputStream(outfile.toPath()), StandardCharsets.UTF_8));
				logMinificationResult(s, infile, outfile);
			} catch (MinificationException | IOException | NoSuchMethodException | SecurityException
					| InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new MojoFailureException("Unable to minify resources.", e);
			}
		}
		return;
	}

	/**
	 * Logs minification result.
	 *
	 * @param name    filename
	 * @param infile  input {@link File}
	 * @param outfile output {@link File}
	 */
	private void logMinificationResult(String name, File infile, File outfile) {
		long pre = infile.length();
		long post = outfile.length();
		long reduction = (long) (100.0 - (((double) post / (double) pre) * 100.0));
		getLog().info("Minified '" + name + "' " + pre + " -> " + post + " (" + reduction + "%)");
		return;
	}
}
