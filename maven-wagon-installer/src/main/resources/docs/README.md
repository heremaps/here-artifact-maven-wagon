# Introduction
This is a tool for installing HERE Technologies' Maven Wagon Plugin so that Maven can download dependencies from HERE Technologies' Artifact Service.

# Usage
1. [Configure your environment](https://developer.here.com/olp/documentation/sdk-developer-guide/dev_guide/topics/configure-your-environment.html)
2. For **Windows**
  * Run *INSTALL.BAT* from your command prompt in your working directory
3. For **Mac/Linux**
  * Run *install.sh* from your shell in your working directory
4. Run `mvn -U --file pom-test.xml clean dependency:resolve` to test. Your installation is successful if there are no errors.

