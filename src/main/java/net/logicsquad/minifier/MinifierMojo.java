package net.logicsquad.minifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    @Parameter(property = "baseSourceDir", required = true)
    private String baseSourceDir;

    @Parameter(property = "baseTargetDir", required = true)
    private String baseTargetDir;

    @Parameter(property = "jsSourceDir")
    private String jsSourceDir;

    @Parameter(property = "jsIncludes")
    private ArrayList<String> jsIncludes;

    @Parameter(property = "jsExcludes")
    private ArrayList<String> jsExcludes;

    @Parameter(property = "jsTargetDir")
    private String jsTargetDir;

    @Parameter(property = "cssSourceDir")
    private String cssSourceDir;

    @Parameter(property = "cssIncludes")
    private ArrayList<String> cssIncludes;

    @Parameter(property = "cssExcludes")
    private ArrayList<String> cssExcludes;

    @Parameter(property = "cssTargetDir")
    private String cssTargetDir;

    private String effectiveJsSourceDir;

    private String effectiveCssSourceDir;

    private String effectiveJsTargetDir;

    private String effectiveCssTargetDir;

    private List<String> jsFilenames;

    private List<String> cssFilenames;

    private String effectiveJsSourceDir() {
    	if (effectiveJsSourceDir == null) {
    		StringBuilder sb = new StringBuilder(baseSourceDir);
    		if (jsSourceDir != null && !jsSourceDir.isEmpty()) {
    			sb.append(File.pathSeparatorChar).append(jsSourceDir);
    		}
    		effectiveJsSourceDir = sb.toString();
    	}
    	return effectiveJsSourceDir;
    }

    private String effectiveCssSourceDir() {
    	if (effectiveCssSourceDir == null) {
    		StringBuilder sb = new StringBuilder(baseSourceDir);
    		if (cssSourceDir != null && !cssSourceDir.isEmpty()) {
    			sb.append(File.pathSeparatorChar).append(cssSourceDir);
    		}
    		effectiveCssSourceDir = sb.toString();
    	}
    	return effectiveCssSourceDir;
    }

    private String effectiveJsTargetDir() {
    	if (effectiveJsTargetDir == null) {
    		StringBuilder sb = new StringBuilder(baseTargetDir);
    		if (jsTargetDir != null && !jsTargetDir.isEmpty()) {
    			sb.append(File.pathSeparatorChar).append(jsTargetDir);
    		}
    		effectiveJsTargetDir = sb.toString();
    	}
    	return effectiveJsTargetDir;
    }

    private String effectiveCssTargetDir() {
    	if (effectiveCssTargetDir == null) {
    		StringBuilder sb = new StringBuilder(baseTargetDir);
    		if (cssTargetDir != null && !cssTargetDir.isEmpty()) {
    			sb.append(File.pathSeparatorChar).append(cssTargetDir);
    		}
    		effectiveCssTargetDir = sb.toString();
    	}
    	return effectiveCssTargetDir;
    }

    private List<String> jsFilenames() {
    	if (jsFilenames == null) {
    		jsFilenames = new ArrayList<>();
    		DirectoryScanner scanner = new DirectoryScanner();
    		scanner.setBasedir(effectiveJsSourceDir());
    		scanner.setIncludes(jsIncludes.toArray(new String[0]));
    		scanner.setExcludes(jsExcludes.toArray(new String[0]));
    		scanner.addDefaultExcludes();
    		scanner.scan();
    		for (String s : scanner.getIncludedFiles()) {
    			jsFilenames.add(s);
    		}
    	}
    	return jsFilenames;
    }

    private List<String> cssFilenames() {
    	if (cssFilenames == null) {
    		cssFilenames = new ArrayList<>();
    		DirectoryScanner scanner = new DirectoryScanner();
    		scanner.setBasedir(effectiveCssSourceDir());
    		scanner.setIncludes(cssIncludes.toArray(new String[0]));
    		scanner.setExcludes(cssExcludes.toArray(new String[0]));
    		scanner.addDefaultExcludes();
    		scanner.scan();
    		for (String s : scanner.getIncludedFiles()) {
    			cssFilenames.add(s);
    		}
    	}
    	return cssFilenames;
    }

	public void execute() throws MojoExecutionException, MojoFailureException {
		for (String s : jsFilenames()) {
			try {
				File infile = new File(effectiveJsSourceDir(), s);
				File outfile = new File(effectiveJsTargetDir(), s);
				getLog().info("Minifying " + infile + " -> " + outfile);
				Minifier jsMin = new JSMinifier(new FileReader(infile));
				jsMin.minify(new FileWriter(outfile));
			} catch (MinificationException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// Unable to read infile
				e.printStackTrace();
			} catch (IOException e) {
				// Unable to write to outfile
				e.printStackTrace();
			}
		}
		
		for (String s : cssFilenames()) {
			try {
				File infile = new File(effectiveCssSourceDir(), s);
				File outfile = new File(effectiveCssTargetDir(), s);
				getLog().info("Minifying " + infile + " -> " + outfile);
				Minifier cssMin = new CSSMinifier(new FileReader(infile));
				cssMin.minify(new FileWriter(outfile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MinificationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return;
	}
}
