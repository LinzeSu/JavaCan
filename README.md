# Introduction to JavaCan

JavaCan is a project that integrates CAN message processing functionalities. Currently supported functions include transferring byte array messages to JSON, making them easy to process in data pipelines.

There hasn't been a Java library that deals with CAN (Controller Area Network) message handling. Nowadays, data 
scientists and analysts are using Java tools to build pipelines for data-related work. The currently available 
libraries are mainly written in Python, such as [cantools](https://github.com/cantools/cantools), which can add more 
complexity to the data system when the pipeline is Java-based. Since what Python can do, Java can do too (JavaCan :D), 
this 
library aims to build CAN message processing related functions that Java users find easy to use.

For examples, see the unit test.

Please note that there is no DBC validation check for now, so you should ensure that the DBC file is correct. There might be a problem if a message contains both big endian and little endian message.

## Future Functionalities

Here are functionalities that are planned to be added:

1. Define message and convert to byte array
2. Add ARXML support
3. Add DBC file check