# TCP File Pusher

TCP File Pusher is a lightweight Java desktop application that simplifies sending files to devices over TCP. It provides a clear graphical interface to select files, target devices, and transfer them reliably — designed for developers and testers who need a fast, dependable way to push files to embedded devices, test rigs, or remote systems on the same network.

## Purpose
The application's primary goal is to make file transfers to devices simple and repeatable. It abstracts connection management, transfer progress, and error reporting so users can focus on deploying and testing files rather than low-level transfer details.

Typical use cases:
- Pushing test binaries or assets to embedded devices during development
- Sending logs or configuration files to remote systems for debugging
- Rapidly deploying files across devices in a QA environment

## Key Features
- Intuitive GUI for selecting targets, files, and transfer options
- Reliable TCP-based transfer with progress and error reporting
- Support for common device workflows and integration hooks
- Cross-platform desktop packaging (distributable installers/packages will be provided separately)
- Lightweight and focused on fast, repeatable file delivery

## Device Integration Notes
- Device operations are performed through reliable TCP connections; where appropriate the app can invoke platform tools or helper scripts to assist with device discovery or preparation.
- Long-running or blocking operations are run off the UI thread and progress/notifications are surfaced in the interface.
- The app provides clear status and error messages to help diagnose connectivity or transfer problems.

## Contributing
Contributions are welcome. If you want to contribute:
- Open an issue to discuss larger changes or proposed features.
- Fork the repository and create a branch for your work.
- Provide clear commit messages and a brief description of changes in pull requests.

If you prefer, open an issue first describing the change so it can be discussed before implementation.

## License
See the LICENSE file in the repository root for license details (or add a LICENSE file if none is present).

## Downloads
Binary downloads and installers will be published separately. When available, download links will be posted here.

## Contact / Repository
Project repository: https://github.com/eliezer-software-enginner/tcp-file-pusher
