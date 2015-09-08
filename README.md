# Mapbox Cordova Plugin
by [Telerik](https://www.telerik.com) / [Eddy Verbruggen](http://twitter.com/eddyverbruggen)

## 0. Index

1. [Description](#1-description)
2. [Screenshots](#2-screenshots)
3. [Installation](#3-installation)
4. [Usage](#4-usage)
5. [License](#5-license)

## 1. Description

Use Mapbox - an OpenGL powered vector-based native mapping solution - in your Cordova / PhoneGap app.

iOS support only, but Android support is just around the corner!

## 2. Screenshots

iOS

<img src="screenshots/ios/ios-marker-amsterdam.png" width="250"/>&nbsp;
<img src="screenshots/ios/ios-location-benelux.png" width="250"/>&nbsp;
<img src="screenshots/ios/ios-location-europe-dark-boxed.png" width="250"/>


## 3. Installation

```
$ cordova plugin add https://github.com/Telerik-Verified-Plugins/Mapbox --variable ACCESS_TOKEN=your.access.token
$ cordova prepare
```

Mapbox.js is brought in automatically. There is no need to change or add anything in your html.


2\. Grab a copy of ActionSheet.js, add it to your project and reference it in `index.html`:
```html
<script type="text/javascript" src="js/ActionSheet.js"></script>
```

3\. Download the source files and copy them to your project.

iOS: Copy the `.h` and `.m` files to `platforms/ios/<ProjectName>/Plugins`

Android: Copy `ActionSheet.java` to `platforms/android/src/nl/xservices/plugins/actionsheet/` (create the folders)

WP8: Copy `ActionSheet.cs` to `platforms/wp8/Plugins/nl.x-services.plugins.actionsheet` (create the folders)

### PhoneGap Build
ActionSheet  works with PhoneGap build too! Just add the following xml to your `config.xml` to always use the latest version of this plugin:
```xml
<gap:plugin name="nl.x-services.plugins.actionsheet" />
```

ActionSheet.js is brought in automatically. Make sure though you include a reference to cordova.js in your index.html's head:
```html
<script type="text/javascript" src="cordova.js"></script>
```

## 4. Usage

Check the [demo code](demo) for all the tricks in the book!


## 5. License

[The MIT License (MIT)](http://www.opensource.org/licenses/mit-license.html)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.