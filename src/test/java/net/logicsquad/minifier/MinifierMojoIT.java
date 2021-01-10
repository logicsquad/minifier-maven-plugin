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
}
