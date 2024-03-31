# NeoTerm API

[![Build status](https://github.com/juic3b0x/neoterm-api/workflows/Build/badge.svg)](https://github.com/juic3b0x/neoterm-api/actions)
[![Join the chat at https://gitter.im/neoterm/neoterm](https://badges.gitter.im/neoterm/neoterm.svg)](https://gitter.im/neoterm/neoterm)

This is an app exposing Android API to command line usage and scripts or programs.

When developing or packaging, note that this app needs to be signed with the same
key as the main NeoTerm app for permissions to work (only the main NeoTerm app are
allowed to call the API methods in this app).

## Installation

NeoTerm:API application can be obtained from [F-Droid](https://f-droid.org/en/packages/io.neoterm.api/).

Additionally we provide per-commit debug builds for those who want to try
out the latest features or test their pull request. This build can be obtained
from one of the workflow runs listed on [Github Actions](https://github.com/juic3b0x/neoterm-api/actions)
page.

Signature keys of all offered builds are different. Before you switch the
installation source, you will have to uninstall the NeoTerm application and
all currently installed plugins. Check https://github.io.neoterm/neoterm-app#Installation for more info.

## License

Released under the [GPLv3 license](http://www.gnu.org/licenses/gpl-3.0.en.html).

## How API calls are made through the neoterm-api helper binary

The [neoterm-api](https://github.com/juic3b0x/neoterm-api-package/blob/main/neoterm-api.c)
client binary in the `neoterm-api` package generates two linux anonymous namespace
sockets, and passes their address to the [NeoTermApiReceiver broadcast receiver](https://github.com/juic3b0x/neoterm-api/blob/main/app/src/main/java/io.neoterm/api/NeoTermApiReceiver.java)
as in:

```
/system/bin/am broadcast ${BROADCAST_RECEIVER} --es socket_input ${INPUT_SOCKET} --es socket_output ${OUTPUT_SOCKET}
```

The two sockets are used to forward stdin from `neoterm-api` to the relevant API
class and output from the API class to the stdout of `neoterm-api`.

## Client scripts

Client scripts which processes command line arguments before calling the
`neoterm-api` helper binary are available in the [neoterm-api package](https://github.com/juic3b0x/neoterm-api-package).

## Ideas

- Wifi network search and connect.
- Add extra permissions to the app to (un)install apps, stop processes etc.
