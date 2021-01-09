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
    /**
     * Webapp source directory.
     */
    @Parameter(property = "sourceDir", defaultValue = "${basedir}/src/main/webserver-resources")
    private String sourceDir;

    /**
     * Webapp target directory.
     */
    @Parameter(property = "targetDir", defaultValue = "${project.build.directory}/${project.build.finalName}.woa/Contents/WebServerResources")
    private String targetDir;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("sourceDir = " + sourceDir);
		getLog().info("targetDir = " + targetDir);

		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(sourceDir);
		scanner.scan();
		List<String> javascripts = new ArrayList<>();
		List<String> cssFiles = new ArrayList<>();
		for (String s : scanner.getIncludedFiles()) {
			getLog().info(s);
			if (s.endsWith(".js")) {
				javascripts.add(s);
			} else if (s.endsWith(".css")) {
				cssFiles.add(s);
			}
		}
		for (String s : javascripts) {
			try {
				File infile = new File(sourceDir, s);
				getLog().info("File: " + infile + " exists: " + infile.exists());
				File outfile = new File(targetDir, s);
				getLog().info("File: " + outfile + " exists: " + outfile.exists());

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
		for (String s : cssFiles) {
			try {
			File infile = new File(sourceDir, s);
			getLog().info("File: " + infile + " exists: " + infile.exists());
			File outfile = new File(targetDir, s);
			getLog().info("File: " + outfile + " exists: " + outfile.exists());

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
