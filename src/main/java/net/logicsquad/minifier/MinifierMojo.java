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
    @Parameter(property = "sourceDir", required = true)
    private String sourceDir;

    @Parameter(property = "targetDir", required = true)
    private String targetDir;

    @Parameter(property = "jsIncludes")
    private ArrayList<String> jsIncludes;

    @Parameter(property = "jsExcludes")
    private ArrayList<String> jsExcludes;

    @Parameter(property = "cssIncludes")
    private ArrayList<String> cssIncludes;

    @Parameter(property = "cssExcludes")
    private ArrayList<String> cssExcludes;

    private List<String> jsFilenames;

    private List<String> cssFilenames;

    private List<String> jsFilenames() {
    	if (jsFilenames == null) {
    		jsFilenames = new ArrayList<>();
    		DirectoryScanner scanner = new DirectoryScanner();
    		scanner.setBasedir(sourceDir);
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
    		scanner.setBasedir(sourceDir);
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
				File infile = new File(sourceDir, s);
				File outfile = new File(targetDir, s);
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
				File infile = new File(sourceDir, s);
				File outfile = new File(targetDir, s);
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
