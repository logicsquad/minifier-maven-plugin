package net.logicsquad.minifier;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

@MavenJupiterExtension
public class MinifierMojoIT {
	@MavenTest
	public void canBuild(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		assertThat(result).project().hasTarget().withFile("hello.css");
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
}
