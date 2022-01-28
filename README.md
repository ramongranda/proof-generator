# R+D and Innovation Evidences Generator

***

<div align="center">
    <b><em>Evidences Generator</em></b><br>
    The simple, R+D and Innovation Evidences Generator 
</div>

<div align="center"> 

![GitHub](https://img.shields.io/github/license/ramongranda/evidences-generator)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/ramongranda/evidences-generator/Develop%20Branch?label=Java%20CLI&logo=github)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/ramongranda/evidences-generator)
![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/ramongranda_evidences-generator/master?server=https%3A%2F%2Fsonarcloud.io) 
![GitHub last commit](https://img.shields.io/github/last-commit/ramongranda/evidences-generator)
    
[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=ramongranda_evidences-generator)](https://sonarcloud.io/summary/new_code?id=ramongranda_evidences-generator)

</div>

***
## Project status

As of  January 1, 2022, Evidences Generator is in develop mode. This means that the java version has been updated. 
This means that the java version has been updated and from this moment on the versions 1.x.x (based on Java 11). 
Please consider upgrading to one of these versions at your earliest convenience.

## Latest news

* 26/01/2022: Evidences generator v1.4.0 supports gitHub repository with Peronal access tokens. [GitHub: How to creating a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) 

# What is Evidences Generator ?
The evidence generator allows the scanning of code repositories in order to generate the necessary documentation for the justification of the evidence of the development of innovation and development projects.

## RUN

java -jar  -Dspring.config.location=repositories.yml evidences-generator-1.4.0.jar <user> <pass>

**user**: Username of repository. The system scans all user commits.
    
**pass**: Repository password. Not used in local repositories and github access with personal access token.

##  Definition of repositories ( repositories.yml)
```yaml
app:
  scanner:
    since: since date (*)
    until: until date (*)
    committer: User's name and surname  
    pon: Project PON code
    black-list:
      - word1
      - word2
    jira-codes:
      -
    exclude-commits:
      - remove me if I am
      - remove
    exclude-files:
      - .yml
      - .xml
      - .txt
      - .doc
      - .json
    git:
      ssl-verify: false
      enabled: true
      repositories:
        - 
          url : https://host/bitbucket/scm/repo1/main.git 
          branches: 
            - develop 
          code: PROJECT NAME
          enabled: false      
        - 
          url : https://host/bitbucket/scm/repo2/main.git 
          branches: 
            - develop 
            - master
          code: PROJECT NAME 2
          enabled: true 
        - 
          url : https://github.com/ramongranda/evidences-generator.git
          token: {{pesonal_access_token}}
          branches: 
            - develop 
            - master
          code: EVIDENCES GENERATOR
          enabled: true           
    svn:
      enabled: false        
      repositories:
        - 
          url: https://host/svn/repo1/main 
          branches: 
            - trunk
          code: PROJECT NAME
          enabled: false
    local:
        enabled: true
        repositories:
         -
           path: Absolute path to where the Git repository was downloaded
           code: PROJECT NAME
           enabled: true
```
(*) format date: DD/MM/YYYY
(**) token: Retrieve repository for github with personal 


## License

The [GPL 3.0](http://opensource.org/licenses/GPL-3.0). See [LICENSE.txt](https://github.com/ramongranda/evidences-generator/master/LICENSE.txt).
