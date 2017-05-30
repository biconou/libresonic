 

#Foreword
Subsonic API documentation page : http://www.subsonic.org/pages/api.jsp


#The RAML design
 
 RAML design file : /libresonic-rest-api/src/main/resources/design/raml/Libresonic-rest-api.raml
 
 Each resource in the API is defined by a block in the RAML file.
 Resources are presented in the same order than here : http://www.subsonic.org/pages/api.jsp
 Groupping by category (ex System)
 
 For exemple : 
 
 ```
 /rest/ping.view:
    displayName: ping
    description: Tests that the API is up and returns the API version
    type: allType
    get:
        is: [ allTrait ]
 
 ```
 
| field       | description                                                                                                                               |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| displayName | Name of the resource to display                                                                                                           |
| description | Description of what the resource does or returns. This description is based on the descriptions found in the Subsonic API document page.  |
 

##SoapUI integration
 
 SoapUI raml plugin : https://github.com/SmartBear/soapui-raml-plugin
 Create an empty project, right click on the project name and choose "import RAML definition"
 
 SoapUI raml limitations : 
 - SoapUI RAML plugin project seems to be alive
 - does not use examples values. On est obligé d'utiliser le champ default.
 - swagger is now SmartBear 
 
##API Workbench

- API Workbench http://apiworkbench.com/

The project is in /libresonic-rest-api/src/main/resources/design/raml/api-workbench-project/

#Swagger

##swaggerhub

https://swaggerhub.com/