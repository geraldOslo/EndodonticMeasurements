This is an example Maven project implementing my Endotontic_Measurements plugin for ImagJ.

I have a working version of this Plugin, and it has already been used in some research:<br>
Jordal, K et al. (2021). Effects of an individualized training course on technical quality and periapical status of teeth treated endodontically by dentists in the Public Dental Service in Norway. An observational intervention study. International Endodontic Journal. ISSN 0143-2885. doi: 10.1111/iej.13669.

But now I try to do it the right way, using Maven, Git, Eclipse for maintaining the plugin. Sadly there is something wrong. When I run the EndoStarter.java for testing the Plugin I get a Class not found error. But the Class-file is in the target folder. I also get an error on declaring OvalRoi in the main file.

I would be so happy if anybody could give me some advice on that

# Purpose of the file:
The Plugin is used to mark landmarks as apex and stores the coordinates in a csv file for further analysis. There are also radiobuttons and free text fields for qualitative observations as PAI stored to the same csv-file. I will add a better description when I get the program to work.

# More on the errors:
Eclipse gives me when running EndoStarterJava.java:
Description	Resource	Path	Location	Type
A class file was not written. The project may be inconsistent, if so try refreshing this project and building it	Process_Pixels.java	/imagej-example-legacy-plugin-c67c81b/src/main/java/com/mycompany/imagej	Unknown	Java Problem
Some Enforcer rules have failed. Look above for specific messages explaining why the rule failed. (org.apache.maven.plugins:maven-enforcer-plugin:3.0.0:enforce:enforce-rules:validate)

org.apache.maven.plugin.MojoExecutionException: Some Enforcer rules have failed. Look above for specific messages explaining why the rule failed.
	at org.apache.maven.plugins.enforcer.EnforceMojo.execute(EnforceMojo.java:255)
	at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo(DefaultBuildPluginManager.java:137)
	at org.eclipse.m2e.core.internal.embedder.MavenImpl.execute(MavenImpl.java:336)
	at org.eclipse.m2e.core.internal.embedder.MavenImpl.lambda$8(MavenImpl.java:1423)
	at org.eclipse.m2e.core.internal.embedder.MavenExecutionContext.executeBare(MavenExecutionContext.java:182)
	at org.eclipse.m2e.core.internal.embedder.MavenExecutionContext.execute(MavenExecutionContext.java:117)
	at org.eclipse.m2e.core.internal.embedder.MavenImpl.execute(MavenImpl.java:1422)
	at org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant.build(MojoExecutionBuildParticipant.java:55)
	at org.eclipse.m2e.core.internal.builder.MavenBuilderImpl.build(MavenBuilderImpl.java:135)
	at org.eclipse.m2e.core.internal.builder.MavenBuilder$1.method(MavenBuilder.java:169)
	at org.eclipse.m2e.core.internal.builder.MavenBuilder$1.method(MavenBuilder.java:1)
	at org.eclipse.m2e.core.internal.builder.MavenBuilder$BuildMethod.lambda$1(MavenBuilder.java:114)
	at org.eclipse.m2e.core.internal.embedder.MavenExecutionContext.executeBare(MavenExecutionContext.java:182)
	at org.eclipse.m2e.core.internal.embedder.MavenExecutionContext.execute(MavenExecutionContext.java:117)
	at org.eclipse.m2e.core.internal.builder.MavenBuilder$BuildMethod.lambda$0(MavenBuilder.java:105)
	at org.eclipse.m2e.core.internal.embedder.MavenExecutionContext.executeBare(MavenExecutionContext.java:182)
	at org.eclipse.m2e.core.internal.embedder.MavenExecutionContext.execute(MavenExecutionContext.java:156)
	at org.eclipse.m2e.core.internal.embedder.MavenExecutionContext.execute(MavenExecutionContext.java:103)
	at org.eclipse.m2e.core.internal.builder.MavenBuilder$BuildMethod.execute(MavenBuilder.java:88)
	at org.eclipse.m2e.core.internal.builder.MavenBuilder.build(MavenBuilder.java:198)
	at org.eclipse.core.internal.events.BuildManager$2.run(BuildManager.java:853)
	at org.eclipse.core.runtime.SafeRunner.run(SafeRunner.java:45)
	at org.eclipse.core.internal.events.BuildManager.basicBuild(BuildManager.java:232)
	at org.eclipse.core.internal.events.BuildManager.basicBuild(BuildManager.java:281)
	at org.eclipse.core.internal.events.BuildManager$1.run(BuildManager.java:334)
	at org.eclipse.core.runtime.SafeRunner.run(SafeRunner.java:45)
	at org.eclipse.core.internal.events.BuildManager.basicBuild(BuildManager.java:337)
	at org.eclipse.core.internal.events.BuildManager.basicBuildLoop(BuildManager.java:389)
	at org.eclipse.core.internal.events.BuildManager.build(BuildManager.java:410)
	at org.eclipse.core.internal.events.AutoBuildJob.doBuild(AutoBuildJob.java:160)
	at org.eclipse.core.internal.events.AutoBuildJob.run(AutoBuildJob.java:251)
	at org.eclipse.core.internal.jobs.Worker.run(Worker.java:63)
	pom.xml	/EndodonticMeasurements	line 8	Maven Build Problem
The constructor OvalRoi(double, double, double, double) is undefined	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 1030	Java Problem
The constructor PointRoi(double, double) is undefined	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 1068	Java Problem

# Warnings
I also get these warnings:
Description	Resource	Path	Location	Type
Enumeration is a raw type. References to generic type Enumeration<E> should be parameterized	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 721	Java Problem
Enumeration is a raw type. References to generic type Enumeration<E> should be parameterized	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 732	Java Problem
Enumeration is a raw type. References to generic type Enumeration<E> should be parameterized	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 741	Java Problem
Enumeration is a raw type. References to generic type Enumeration<E> should be parameterized	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 1011	Java Problem
Overriding managed version 1.53f for ij	pom.xml	/EndodonticMeasurements	line 93	Maven pom Loading Problem
The import ij.ImagePlus is never used	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 28	Java Problem
The static method getName(String) from the type RoiManager should be accessed in a static way	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 980	Java Problem
The static method setColor(Color) from the type Roi should be accessed in a static way	Endodontic_Measurements.java	/EndodonticMeasurements/src/main/java/no/uio/odont/imagej	line 971	Java Problem

