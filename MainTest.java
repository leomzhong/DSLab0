import java.io.File;
import java.io.FileInputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class MainTest {
	public static void main(String[] args) {
		Yaml test = new Yaml(new Constructor(Configuration.class));
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(new File("testConfig.yml"));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			return;
		}
		Configuration config = (Configuration)test.load(stream);
		System.out.println(config.toString());
	}
}
