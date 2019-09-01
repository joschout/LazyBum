package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class ProgramConfiguration {

    private Properties properties;

    public ProgramConfiguration(String propertiesFilePath) throws IOException, ImproperProgramConfigurationException {

        properties = new Properties();
        FileInputStream in = new FileInputStream(propertiesFilePath);
        properties.load(in);
        in.close();

        for(ProgramConfigurationOption configOption: ProgramConfigurationOption.values()){
            if(properties.getProperty(configOption.toString()) == null){
                PrintWriter writer = new PrintWriter(System.out);
                String message = "Did not find a configuration value for: " + configOption.toString();
                writer.write(message);
                writer.close();
                throw new ImproperProgramConfigurationException(message);
            }
        }
    }

    public String getConfigurationOption(ProgramConfigurationOption option){
        return properties.getProperty(option.toString());
    }

    public void setConfigurationOption(ProgramConfigurationOption option, String value){
        properties.setProperty(option.toString(), value);
    }


    public String toString(){
        String str = "";

        for(ProgramConfigurationOption option: ProgramConfigurationOption.values()){
            if(! option.equals(ProgramConfigurationOption.DB_PASSWORD)){
                str += option.toString() + " = " + properties.getProperty(option.toString());
                str += "\n";
            }
        }
        return str;
    }

}
