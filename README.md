# Speed Grader
An application for automating the project evaluating process for teaching assistants and professors in the Canyons' Computer
Science program.

NOTE: Currently the program cannot successfully execute the student java projects. Instead, the command string used to
create a java runtime process is logged to the output file for easy copy/paste execution.

<img src="https://i.imgur.com/SwAdrs4.png" width="300">

---

## Usage
It will be necessary to ensure that the `ant` program is running correctly on your computer, if you are using Netbeans
you can find the path to your ant bin through the IDEs Java preferences. (System should have a path to ant home by
default)
* this path will be saved to a config.properties file across runtime instances, so entering this ant path is only required once.

After selecting number of test inputs and iterations, the generate button will create text fields to enter test inputs.

By checking the "User Input" box, you can specify that the student projects will take user input instead of using command
line arguments (CLA).

The input text file created for input redirection and the output text field used to write project outputs will be saved
to the "Class Projects Directory".

<img src="https://i.imgur.com/AP5ufTG.png" width="300">

---

## Included Dependencies
- zip4j
- commons-io
