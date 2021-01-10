package net.logicsquad.minifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

@Mojo(name = "minify", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MinifierMojo extends AbstractMojo {
	@Parameter(property = "sourceDir", required = true)
	private String sourceDir;

	@Parameter(property = "targetDir", required = true)
	private String targetDir;

	@Parameter(property = "jsIncludes")
	private List<String> jsIncludes;

	@Parameter(property = "jsExcludes")
	private List<String> jsExcludes;

	@Parameter(property = "cssIncludes")
	private List<String> cssIncludes;

	@Parameter(property = "cssExcludes")
	private List<String> cssExcludes;

	private List<String> jsFilenames;

	private List<String> cssFilenames;

	private List<String> jsFilenames() {
		if (jsFilenames == null) {
			jsFilenames = new ArrayList<>();
			initialiseFilenames(jsFilenames, jsIncludes, jsExcludes);
		}
		return jsFilenames;
	}

	private List<String> cssFilenames() {
		if (cssFilenames == null) {
			cssFilenames = new ArrayList<>();
			initialiseFilenames(cssFilenames, cssIncludes, cssExcludes);
		}
		return cssFilenames;
	}

	private void initialiseFilenames(List<String> list, List<String> includes, List<String> excludes) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(sourceDir);
		scanner.setIncludes(includes.toArray(new String[0]));
		scanner.setExcludes(excludes.toArray(new String[0]));
		scanner.addDefaultExcludes();
		scanner.scan();
		for (String s : scanner.getIncludedFiles()) {
			list.add(s);
		}
		return;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		minify(JSMinifier.class, jsFilenames());
		minify(CSSMinifier.class, cssFilenames());
		return;
	}

	private void minify(Class<? extends Minifier> minifierClass, List<String> filenames) throws MojoFailureException {
		for (String s : filenames) {
			try {
				File infile = new File(sourceDir, s);
				File outfile = new File(targetDir, s);
				Constructor<? extends Minifier> constructor = minifierClass.getConstructor(Reader.class);
				Minifier minifier = constructor.newInstance(new FileReader(infile));
				minifier.minify(new FileWriter(outfile));
				logMinificationResult(s, infile, outfile);
			} catch (MinificationException | IOException | NoSuchMethodException | SecurityException
					| InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new MojoFailureException("Unable to minify resources.", e);
			}
		}
		return;
	}

	private void logMinificationResult(String name, File infile, File outfile) {
		long pre = infile.length();
		long post = outfile.length();
		long reduction = (long) (100.0 - (((double) post / (double) pre) * 100.0));
		getLog().info("Minified '" + name + "' " + pre + " -> " + post + " (" + reduction + "%)");
		return;
	}
}
