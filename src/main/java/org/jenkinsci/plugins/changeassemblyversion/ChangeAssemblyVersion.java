package org.jenkinsci.plugins.changeassemblyversion;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import hudson.EnvVars;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.EnvironmentList;
import hudson.model.EnvironmentContributor;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author <a href="mailto:leonardo.kobus@hbsis.com.br">Leonardo Kobus</a>
 */
public class ChangeAssemblyVersion extends Builder{
	
	private final String task;
	
	@DataBoundConstructor
	public ChangeAssemblyVersion(String task){
		this.task = task;
	}
	
	public String getTask(){
		return this.task;
	}
	
	/**
	 * 
	 * The perform method is gonna search all the file named "Assemblyinfo.cs" in any folder below,
	 * and after found it is gonna change the version of AssemblyVersion and AssemblyFileVersion in the
	 * file for the inserted version (task property value).
	 * 
	 * 
	 * OBS: The inserted value can be some jenkins variable like ${BUILD_NUMBER} just the variable alone, but not implemented to treat 0.0.${BUILD_NUMBER}.0
	 * I think this plugin must be used with Version Number Plugin.
	 * 
	 **/
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {    	
    	try{
    		EnvVars envVars = new EnvVars();
        	envVars = build.getEnvironment(listener);	               
	        String version = new AssemblyVersion(this.task, envVars).getVersion();
	        listener.getLogger().println(String.format("Changing the AssemblyInfo.cs to version : %s", version));
	        List<FilePath> fp = build.getWorkspace().child(envVars.get("WORKSPACE")).list();
	        ChangeTools.ReplaceAllProperties(fp, version, listener);
    	}catch(Exception ex){    		
    		StringWriter sw = new StringWriter();
    		ex.printStackTrace(new PrintWriter(sw));    		
    		listener.getLogger().println(sw.toString());    		    		
    		return false;
    	}
        return true;
    }
	
	@Extension
	public static class Descriptor extends BuildStepDescriptor<Builder>{
				
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType){
			return FreeStyleProject.class.isAssignableFrom(jobType);
		}
		
		@Override
        public String getDisplayName() {
            return "Change Assembly Version";
        }
	}
	
}
