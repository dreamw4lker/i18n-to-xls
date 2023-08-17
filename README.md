# JSON i18n <--> XLS converter

## About
This project can be used to convert internationalization strings from JSON files used by [vue-i18n](https://kazupon.github.io/vue-i18n/) library to XLS-file and vice versa.

You can convert your JSON files to XLS using this project, then send this XLS file to the translator. When the translator completes the translations in this XLS file, you can convert it back to separate JSON files.

## Build

Build instruments:
* Java 11
* Maven

Run command from root project directory: `mvn clean install`

Result JAR-file: `<root project dir>/target/i18n-xls-converter-<version>-jar-with-dependencies.jar`

## Run

This project can run in two modes: **JSON-to-XLS** and **XLS-to-JSON**.

All startup parameters should be set in console or startup script. Example: `java -jar i18n-xls-converter-<version>-jar-with-dependencies.jar mode=json2xls from=ru-RU to=en-US <...>`. Running mode (`mode=json2xls` or `mode=xls2json`) is the required parameter. 

**Java 11** should be used!

### JSON-to-XLS mode

Required parameters:
* **path** - a path to folder with JSON files (translations). Example: `path=/home/as/projects-idea/isee/web/src/lang`. **Attention!** This folder ususally contains subfolders `ru-RU`, `en-US`, etc. You **should not** include these folders in this parameter.
* **from** - **source** language. It must match the name of subfolder in folder from **path** parameter. Example: `from=ru-RU`
* **to** - **destination** language. It must match the name of subfolder in folder from **path** parameter. Example: `to=en-US`

Additional parameters:
* **password** - a password for the result XLS file. Default value: `12345678`
* **resultFileName** - a name of the result XLS file (file extension should be included). Default value: `translation_template.xls`

Successful completion will result with one XLS file with the name specified in `resultFileName` parameter, and located next to project `.jar` file.

### XLS-to-JSON mode

Required parameters:
* **path** - a path to folder with translations. Example: `path=/home/as/projects-idea/isee/web/src/lang`. **Attention!** You **should not** include `ru-RU`, `en-US`, etc. folders in this parameter.
* **to** - **destination** language into which the translation was made. It must match the name of subfolder in folder from **path** parameter. Example: `to=en-US`
* **xls** - a path to the XLS file with the completed translation. Example: `xls=/home/as/projects-idea/i18n-xls-converter/translation_template.xls`

Successful completion will result the folder `<path>/<to>` with JSON files with translations. All existing files in this folder will be overwritten.
