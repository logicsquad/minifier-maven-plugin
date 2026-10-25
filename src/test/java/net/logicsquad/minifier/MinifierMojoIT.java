package net.logicsquad.minifier;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import java.nio.charset.StandardCharsets;
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

	/**
	 * A resource that isn't valid in the encoding, UTF-8 by default, fails the build,
	 * saying where, rather than being changed.
	 */
	@MavenTest
	public void notValidUtf8(MavenExecutionResult result) {
		assertThat(result).isFailure();
		assertThat(result).out().error()
				.anyMatch(line -> line.contains("Unable to minify 'style.css': Not valid UTF-8 at line 1, column 27"));
		assertThat(result.getMavenProjectResult().getTargetProjectDirectory().resolve("target/web/style.css"))
				.doesNotExist();
		return;
	}

	/**
	 * Resources in another encoding keep their non-ASCII characters, and one that
	 * starts with a byte order mark is still read in the encoding that gives.
	 */
	@MavenTest
	public void otherEncoding(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		Path web = result.getMavenProjectResult().getTargetProjectDirectory().resolve("target/web");
		assertThat(web.resolve("style.css"))
				.hasBinaryContent(".a::before{content:\"caf\u00e9\"}".getBytes(StandardCharsets.ISO_8859_1));
		assertThat(web.resolve("app.js")).hasBinaryContent("var s=\"caf\u00e9\";".getBytes(StandardCharsets.ISO_8859_1));
		assertThat(web.resolve("utf16.css"))
				.hasBinaryContent("\ufeff.a::before{content:\"caf\u00e9\"}".getBytes(StandardCharsets.UTF_16LE));
		return;
	}

	/**
	 * As in browsers, a resource that starts with a byte order mark is read, and
	 * written, in the encoding that gives, with no encoding set.
	 */
	@MavenTest
	public void byteOrderMarks(MavenExecutionResult result) {
		assertThat(result).isSuccessful();
		Path web = result.getMavenProjectResult().getTargetProjectDirectory().resolve("target/web");
		String css = "\ufeff.a::before{content:\"caf\u00e9\"}";
		assertThat(web.resolve("utf8.css")).hasBinaryContent(css.getBytes(StandardCharsets.UTF_8));
		assertThat(web.resolve("utf16be.css")).hasBinaryContent(css.getBytes(StandardCharsets.UTF_16BE));
		assertThat(web.resolve("utf16le.css")).hasBinaryContent(css.getBytes(StandardCharsets.UTF_16LE));
		assertThat(web.resolve("utf16le.js"))
				.hasBinaryContent("\ufeffvar s=\"caf\u00e9\";".getBytes(StandardCharsets.UTF_16LE));
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
