# Introduction to JavaCan

JavaCan is a project that integrates CAN message processing functionalities. Currently supported functions include transferring byte array messages to JSON, making them easy to process in data pipelines.

There hasn't been a Java library that deals with CAN (Controller Area Network) message handling. Nowadays, data 
scientists and analysts are using Java tools to build pipelines for data-related work. The currently available 
libraries are mainly written in Python, such as [cantools](https://github.com/cantools/cantools), which can add more 
complexity to the data system when the pipeline is Java-based. Since what Python can do, Java can do too (JavaCan :D), 
this 
library aims to build CAN message processing related functions that Java users find easy to use.

Please note that there is no DBC validation check for now, so you should ensure that the DBC file is correct. There might be a problem if a message contains both big endian and little endian message.

## Add to dependencies
Use following snippet to add JavaCan to your maven dependency:
```xml
<dependency>
    <groupId>io.github.linzesu</groupId>
    <artifactId>javacan</artifactId>
    <version>1.2.2</version>
</dependency>
```

## Usage
The below code snippet shows the basic use. For more examples, see the [test file](https://github.com/LinzeSu/JavaCan/blob/master/src/test/java/com/linzesu/javacan/JavaCanTest.java). You can find the dbc file used in the code [here](https://github.com/LinzeSu/JavaCan/blob/master/src/test/resources/example-can.dbc).
### Convert byte array CAN message to JSON Object
```java
import com.linzesu.javacan.utils.DBCParser;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import static com.linzesu.javacan.utils.MessageParser.processCanMessageToJSON;

public class HelloWorld {
    public static void main(String[] args) throws IOException {

        // Path of DBC file
        DBCParser dbcParser = new DBCParser("src/test/resources/example-can.dbc");

        // Manually create a message
        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        byte[] byteMessage = new byte[]{0x01, 0x20, 0x00, 0x03, 0x40, 0x00, 0x05, 0x00};
        boolean isExtended = false;

        // Choose the signals you want to parse. If using null, all signals will be parsed.
        ArrayList<String> signalName = new ArrayList<>();
        signalName.add("Cat");
        signalName.add("Dog");

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParser.getMessageDefinitions(),byteId,
                byteMessage, isExtended, signalName);

        // You should see {"Cat":2,"Dog":1} printed in the console
        System.out.println(CanMessageInJson);

    }
}

```

## Future Functionalities

Here are functionalities that are planned to be added:

- [x] Convert CAN message in JSON to byte array 
- [ ] Add ARXML support 
- [ ] Add DBC file check 
- [ ] Add CANFD support