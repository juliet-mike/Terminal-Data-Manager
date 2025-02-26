# Lanterna Menu System Documentation

## Overview
The Lanterna menu system provides a way to create interactive text-based user interfaces using Java. It consists of three primary components:

- **MenuBar**: A top-level container that holds multiple menus.
- **Menu**: A drop-down menu that contains multiple menu items or submenus.
- **MenuItem**: An individual selectable item within a menu.

## Components

### MenuBar
The `MenuBar` is a container for `Menu` objects and provides navigation for the user.

#### Creating a MenuBar
```java
MenuBar menuBar = new MenuBar();
```

#### Adding Menus to the MenuBar
```java
Menu fileMenu = new Menu("File");
Menu editMenu = new Menu("Edit");
menuBar.add(fileMenu).add(editMenu);
```

### Menu
A `Menu` represents a drop-down list of menu items.

#### Creating a Menu
```java
Menu fileMenu = new Menu("File");
```

#### Adding Items to a Menu
```java
fileMenu.add(new MenuItem("Open", () -> System.out.println("Open selected")));
fileMenu.add(new MenuItem("Save", () -> System.out.println("Save selected")));
```

#### Adding a Submenu
```java
Menu settingsMenu = new Menu("Settings");
settingsMenu.add(new MenuItem("Preferences", () -> System.out.println("Preferences selected")));
fileMenu.add(settingsMenu);
```

### MenuItem
A `MenuItem` is an interactive element inside a `Menu`.

#### Creating a MenuItem
```java
MenuItem exitItem = new MenuItem("Exit", () -> System.out.println("Exiting..."));
```

#### Handling MenuItem Selection
```java
fileMenu.add(new MenuItem("Exit", () -> System.exit(0)));
```

## Example: Creating a Simple Menu Bar
```java
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.menu.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

public class MenuExample {
    public static void main(String[] args) throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();
        WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
        Window window = new BasicWindow("Lanterna Menu Example");
        
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.add(new MenuItem("Open", () -> System.out.println("Open selected")));
        fileMenu.add(new MenuItem("Save", () -> System.out.println("Save selected")));
        fileMenu.add(new MenuItem("Exit", () -> System.exit(0)));
        
        menuBar.add(fileMenu);
        
        Panel panel = new Panel();
        panel.addComponent(menuBar);
        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }
}
```

## Conclusion
The Lanterna menu system provides an easy-to-use framework for building interactive text-based menus in Java applications. The `MenuBar`, `Menu`, and `MenuItem` components enable structured navigation and user interaction.

