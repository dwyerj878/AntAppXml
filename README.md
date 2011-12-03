APP-XML
======

App-XML is an ant task specifically designed to dynamically build an application.xml file for J2EE projects.

The project was started to solve the problem of using dynamically resolved dependencies from a tool such as ivy which may include version numbers. 


* Sample :

 	<target name="use" description="Use the Task to create Application.xml" depends="jar">
		<taskdef name="AppXml" classname="net.dev.jcd.ant.app.AppXml" classpath="${ant.project.name}.jar" />
		<AppXml appxml="application.xml" displayname="application-name" librarydirectory="libs" j2eeversion="6">
			<module type="ejb">
				<fileset dir="modules">
					<include name="module*.jar" />
				</fileset>
			</module>
			
			<module type="java">
				<fileset dir="modules">
					<include name="java-module*.jar" />
				</fileset>
			</module>

			<module type="war" contextRoot="webRoot">
				<fileset dir="modules">
					<include name="web-module*.war" />
				</fileset>
			</module>

		</AppXml>

	</target>