===================<br/>
# LOGICALDOC COMMUNITY EXTENSIONS
===================<br/>
Plugin extensions for LogicalDOC Community<br/>

LogicalDOC Community Extensions add new features to version 8.9.1 of [LogicalDOC](https://www.logicaldoc.com).<br/>
In particular there are 3 new plugins that provide the following features:

1. logicaldoc-ce-parser (extracting text from many types of office documents)
2. logicaldoc-ce-ocr (OCR - extract text from images and PDFs for full-text searching)
3. logicaldoc-ce-converter (conversion of documents to other formats and for previews/thumbnail)

It is possible to install these extensions on a LogicalDOC Community 8.9.1 by extracting the contents of the .zip archives in the folder
tomcat/ROOT of your LogicalDOC installation

These 3 plugins using third party software for OCR, image processing and format conversion in particular:
LibreOffice, Tesseract, ImageMagick and GhostScript.

For information on how to install third-party software, refer to the following addresses.

Windows
http://docs.logicaldoc.com/en/installation/install-on-windows/install-third-party-software-windows

Linux
http://docs.logicaldoc.com/en/installation/install-on-linux/install-third-party-software-linux

Ubuntu
http://docs.logicaldoc.com/en/installation/install-on-ubuntu/install-third-party-software-ubuntu


Tesseract:<br/>
after installing Tesseract, configure the path of the executable in the LogicalDOC configuration file /conf/context.properties by modifying the properties<br/>
```
ocr.Tesseract.path=C:\\LogicalDOC\\tesseract\\tesseract.exe
ocr.enabled=true
```

LibreOffice: <br/>
after installing Tesseract, configure the path of the executable in /conf/context.properties by modifying the properties<br/>
```
converter.LibreOfficeConverter.enabled=true
converter.LibreOfficeConverter.path=C:\\Program Files\\LibreOffice
```

Note: remember that from version 8.8.3 it is necessary to set the commands that LogicalDOC can launch on the operating system in the /conf/allowed-commands.txt file

### License
[LogicalDOC Community Edition](https://www.logicaldoc.com/download-logicaldoc-community) is made available to the Open Source community under the [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html).
The LogicalDOC source code is available for the entire community, which is free to use, modify and redistribute under the premises of such license.
