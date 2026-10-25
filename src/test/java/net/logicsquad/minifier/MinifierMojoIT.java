package net.logicsquad.minifier;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import java.nio.file.Path;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

@MavenJupiterExtension
public class MinifierMojoIT {
	/**
	 * Only comments that start with "/*!" are kept.
	 */
	@MavenTest
	public void canBuild(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		assertThat(result).project().hasTarget().withFile("hello.css").content().contains("/*! Banner-style comment")
				.doesNotContain("/**");
		return;
	}

	/**
	 * Maven builds the module from the parent directory, so relative paths must
	 * resolve against the module rather than the working directory.
	 */
	@MavenTest
	public void relativePathsInModule(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		assertThat(result).project().withModule("web").hasTarget().withFile("web/module.css").isNotEmptyFile();
		return;
	}

	@MavenTest
	public void missingSourceDir(MavenExecutionResult result) {
		assertThat(result).isFailure();
		assertThat(result).out().error().anyMatch(line -> line.contains("sourceDir is not an existing directory"));
		return;
	}

	/**
	 * A skipped execution does nothing, not even check sourceDir, which doesn't
	 * exist here.
	 */
	@MavenTest
	@SystemProperty(value = "minifier.skip", content = "true")
	public void skip(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		assertThat(result).out().info().contains("Skipping minification");
		assertThat(result.getMavenProjectResult().getTargetProjectDirectory().resolve("target/web")).doesNotExist();
		return;
	}

	@MavenTest
	public void inPlace(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		Path web = result.getMavenProjectResult().getTargetProjectDirectory().resolve("src/main/web");
		assertThat(web.resolve("style.css")).content().contains("color:#fff");
		assertThat(web.resolve("script.js")).content().contains("return a+b");
		return;
	}

	/**
	 * A resource that can't be minified fails the build, saying why and where, and
	 * is left as it is, even when minifying in place.
	 */
	@MavenTest
	public void failedMinificationInPlace(MavenExecutionResult result) {
		assertThat(result).isFailure();
		assertThat(result).out().error()
				.anyMatch(line -> line.contains("Unable to minify 'style.css': Unterminated comment at line 4, column 1"));
		Path web = result.getMavenProjectResult().getTargetProjectDirectory().resolve("src/main/web");
		assertThat(web.resolve("style.css")).content().isEqualTo(".kept {\n  color: #FFFFFF;\n}\n/* Never closed\n");
		return;
	}

	@MavenTest
	public void emptyResource(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		assertThat(result).out().info().anyMatch(line -> line.startsWith("Minified 'empty.css' 0 -> "))
				.noneMatch(line -> line.startsWith("Minified 'empty.css'") && line.contains("%"));
		return;
	}
}
