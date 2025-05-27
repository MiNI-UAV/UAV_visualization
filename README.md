## Description

Visualization for the Mini UAV BEng thesis project. It is a feature-rich physics engine for UAV research with accompanying multi-player graphics engine built from the ground up in OpenGL.

### Features
 - Multi-player functionality with standalone server and multiple clients connecting through TCP.
 - It is possible to upload 3D drone models and custom maps.
 - Wide variety of UAV parameters. It is possible to design multicopters, planes or rockets with programmable control systems.
 - Compatible with any connectible controller. From gamepads to professional ones.
 - PBR graphics.
 - Exhaustive UI including radar, artificial horizon and a map.

## Abstract

The following program is a system designed to simulate flight dynamics of unmanned aerial vehicles. The system allows real-time flight simulation as well
as three-dimensional visualization. During the flight we log the simulation data, allowing for
flight analysis. A universal dynamics model has been developed to allow highly customizable
aircraft parameters. Those include its mechanical and aerodynamic properties as well as the
configuration of propulsion units and the influence of external factors. The dynamics simulation
was extended to include the control system. The system has been designed in a way to make
it easy to change ship and simulation parameters, create new configurations, and create and
tune control systems. Examples of models that can be simulated include fixed-wind aircraft,
multicopters and rockets.

## Compilation

Install [Gradle](https://gradle.org/).

Run `gradle run` in the root directory.

Make sure the [UAV-Server](https://github.com/MiNI-UAV/UAV_server) is running and server addres `serverSettings.serverAddress` is set correctly in `config.yaml`.

## Video demo

[![Mini-UAV trailer](http://img.youtube.com/vi/NdrdWuIZauQ/0.jpg)](http://www.youtube.com/watch?v=NdrdWuIZauQ "Mini-UAV Trailer")

## Screenshots

![image](https://github.com/user-attachments/assets/08c8745c-2a1e-4562-a1f6-1546e6d9387e)

![image](https://github.com/user-attachments/assets/32f51cf0-3173-4e8e-8d9e-1c7c4fd2f669)

![Zrzut ekranu z 2024-11-07 15-29-19](https://github.com/user-attachments/assets/1a3fae11-0540-408c-8891-63ecb309e039)

![image](https://github.com/user-attachments/assets/a4289b3e-86d2-4570-b96f-476e973c979f)
