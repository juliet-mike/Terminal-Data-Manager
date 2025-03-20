<h1>Terminal Data Manager V0.3</h1>

![{F2917E28-39C4-4066-8C2C-65F5A17839CE}](https://github.com/user-attachments/assets/3c79a2c1-803e-4d56-a271-7bf70bd01993)

no compiled package currently published so you need maven installed on your system to use TDM


```​ git clone https://github.com/juliet-mike/Terminal-Data-Manager.git ```​ 



```​ cd /Terminal-Data-Manager/ ```​



```​  mvn package ```​



your compiled program will then be in the /target direcory inside of the cloned repo file

<h2> TDM is a simple terminal based data manager for dumping and manipulating CSV files and SQL databases and allowing simple spreadsheet CRUD operations.
It can be run from either the shell or as a graphical application on windows, linux or mac with no modification. it will automatically detect which of these two modes it intializes in based on how you start the program.  if using the run.sh or run.bat tdm will start in shell mode but if run from your file browser how you would any other program it will start in GUI mode.

TDM was written to make editing table data from the terminal easier specifically for those working with only a serial console or terminal shell.

Python based extensions coming soon with an internal script editor and runner using GraalPy<h2/> 
