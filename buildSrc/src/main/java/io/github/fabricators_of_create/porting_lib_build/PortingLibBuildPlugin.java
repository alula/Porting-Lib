package io.github.fabricators_of_create.porting_lib_build;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskContainer;

public class PortingLibBuildPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.afterEvaluate(p -> {
			setupDeduplication(p);
			setupResourceProcessing(p);
		});
	}

	public void setupDeduplication(Project project) {
		Task remapJar = project.getTasks().findByName("remapJar");
		if (remapJar == null) {
			throw new IllegalStateException("No remapJar task?");
		}
		Task deduplicateInclusions = project.getTasks().create("deduplicateInclusions", DeduplicateInclusionsTask.class);
		remapJar.finalizedBy(deduplicateInclusions);
		deduplicateInclusions.getInputs().files(remapJar.getOutputs().getFiles());
	}

	public void setupResourceProcessing(Project project) {
		if (project.getRootProject() == project) {
			return; // do not modify the root resources
		}
		TaskContainer tasks = project.getTasks();
		Task processResources = tasks.findByName("processResources");
		if (processResources == null) {
			throw new IllegalStateException("No processResources task?");
		}

		Task expandFmj = tasks.create("expandFmj", ExpandFmjTask.class);
		Task addIcons = tasks.create("addMissingIcons", AddMissingIconsTask.class);
		processResources.finalizedBy(expandFmj, addIcons);
		FileCollection processedResources = processResources.getOutputs().getFiles();
		expandFmj.getInputs().files(processedResources);
		addIcons.getInputs().files(processedResources);
	}
}
