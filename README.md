# Muse-CGH
### A program to convert text into human-style Computer Generated Handwritings.

#### you can [download the compiled program (v1.1)](https://github.com/MrVPlussOne/Muse-CGH/releases/download/v1.1/Muse1.1.zip) directly (.jar file)

### Overview

##### Muse can generate human-style English handwriting from text. Its algorithm connects the strokes of adjacent characters to produce cursive words, and uses randomness to ensure that every character is unique in the rendering results. 

##### The following photo shows some rendered text printed on papers. As you can see, the result is quite vivid:

![alt tag](Printed.jpg)


### Usage

##### Muse can work in two modes: GUI mode and Command-line mode. 
 - The GUI mode is the easy way to use Muse, as it provides interactive editing experience. Just enter the text, click the 'render' button, and you will see the rendered result immediately. If you are not satisfied with the result, just adjust parameters on the control panel, the result then updates accordingly.
 - The Command-line mode can be very helpful if you plan to use Muse in non-Scala projects.

#### GUI mode introduction

##### If you run Muse directly (by double click on the .jar file or by providing no command-line arguments), the program will start in GUI mode.

![alt tag](Sample.png)

##### In the image shown above, the upper right panel is Muse's Result Panel, where the rendered image or animation is presented; the upper left panel is Muse's Control Panel, a place you enter text or adjust parameters; the buttom left panel is the Console Output Panel, it shows information about Muse's state.

 - At the bottom of Control Panel, there're two checkboxes. If you check the 'Interactive' box, everytime you change the text content, without clicking 'Render' button, the Result Panel will update automatically. If you check the 'Animation' box, Result Panel will change into animation mode, then you can watch Muse writing in action!
 
 - At the top of Control Panel, there're two buttons. Click the 'Font Editor' button to bring up the font editor. You can create new Muse Characters of modify the existent ones as you wish. After saving your changes in the font editor, click the 'Reload Letters' button in the Control Panel to let Muse reload character map from disk. (Muse load its characters from a folder named 'letters' in its current directory)

 - As you can see, there're quite many parameters you can change. Help texts will show up if you hover mouse cursor on them.

##### The font editor is still very simple and crude. It's made for fast prototyping at the early stage of this project. So the experience may be not satisfactory. Future improvements're needed.

![alt tag](Editor_Screenshot.png)

#### This work is under [the MIT license](LICENSE.txt), so feel free to use it in your own projects. There's a plan for command line-interface support.
