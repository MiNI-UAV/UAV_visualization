import os.path
import subprocess
from shutil import copy2
from time import sleep
import zmq

drone_statuses = {
    "idle_drone_status": "1,0.006,0,0,0,1,-0,0,0,-0,-0,0.069,-0,0,0,10.053,9.786,9.819,9.944;"
}


def perform_visualization_tests(java_home, visualization_dir, visualization_binary, assets_checksum):
    visualization_address = "127.0.0.1"
    drone_requester_socket, drone_status_socket, drone_steering_socket, drone_utils_socket = set_up_sockets(
        visualization_address)

    set_up_visualization(java_home, visualization_dir, visualization_binary, drone_requester_socket, assets_checksum)

    while True:
        server_respond_to_heartbeat(drone_utils_socket)
        server_respond_to_joystick(drone_steering_socket)
        server_send_drone_status(drone_status_socket, drone_statuses["idle_drone_status"])
        sleep(0.01)


def set_up_sockets(visualization_address):
    context = zmq.Context()
    drone_requester_port = 9000
    drone_requester_socket = context.socket(zmq.REP)
    drone_requester_socket.bind(f"tcp://{visualization_address}:{drone_requester_port}")

    drone_status_port = 9090
    drone_status_socket = context.socket(zmq.PUB)
    drone_status_socket.bind(f"tcp://{visualization_address}:{drone_status_port}")

    drone_steering_port = 10000
    drone_steering_socket = context.socket(zmq.REP)
    drone_steering_socket.bind(f"tcp://{visualization_address}:{drone_steering_port}")

    drone_utils_port = 11000
    drone_utils_socket = context.socket(zmq.PAIR)
    drone_utils_socket.bind(f"tcp://{visualization_address}:{drone_utils_port}")

    return drone_requester_socket, drone_status_socket, drone_steering_socket, drone_utils_socket


def set_up_visualization(java_home, visualization_path, visualization_binary, drone_requester_socket, assets_checksum):
    config_path = os.path.join(visualization_path, "config.yaml")
    temp_config_path = os.path.join(visualization_path, "config.yaml.temp")
    if os.path.exists(config_path):
        os.rename(config_path, temp_config_path)

    test_config_src = os.path.join(".", "test_data", "config.yaml")
    copy2(test_config_src, visualization_path)

    test_config_src = os.path.join(".", "test_data", "test_bindings.yaml")
    copy2(test_config_src, visualization_path)

    test_drone_src = os.path.join(".", "test_data", "testcopter.xml")
    test_drone_dst = os.path.join(visualization_path, "drones")
    os.makedirs(test_drone_dst, exist_ok=True)
    copy2(test_drone_src, test_drone_dst)
    print("Copied test data")

    java_path = os.path.join(java_home, "bin", "java")
    subprocess.Popen([java_path, "-jar", visualization_binary], cwd=visualization_path)
    print("Started UAV_Visualization process")

    set_up_connection(drone_requester_socket, assets_checksum)
    print("Set up connections")

    os.remove(config_path)
    os.remove(os.path.join(test_drone_dst, "testcopter.xml"))
    os.remove(os.path.join(visualization_path, "test_bindings.yaml"))
    if os.path.exists(temp_config_path):
        os.rename(temp_config_path, config_path)
    print("Removed test data")


def set_up_connection(drone_requester_socket, assets_checksum):
    server_set_up_info(drone_requester_socket, assets_checksum)
    server_set_up_drone_config(drone_requester_socket)
    server_set_up_spawn_drone(drone_requester_socket)


def server_set_up_info(socket, assets_checksum):
    message = socket.recv().decode("UTF-8")
    if message != "i":
        raise Exception(f'First message from visualization should be "i" but was "{message}"')
    print(f"[Mock Server] Recv: {message}")
    server_info_message = ('{"checksum":"' + assets_checksum + '","configs":['
                           '"b4788e8f","8249d46f","6e841487","5e03db5a"],"map":"de_dust2"}')
    socket.send(server_info_message.encode("UTF-8"))
    print(f"[Mock Server] Sent: {server_info_message}")


def server_set_up_drone_config(socket):
    server_expected_drone_config = 'c:<params>\n\t<name>test_configuration</name>\n</params>\n'
    message = socket.recv().decode("UTF-8")
    if message != server_expected_drone_config:
        raise Exception(f'Drone config message from visualization should be "{server_expected_drone_config}" '
                        f'but was "{message}"')
    print(f"[Mock Server] Recv: {message}")

    server_drone_config_response = "ok;b4788e8f"
    socket.send(server_drone_config_response.encode("UTF-8"))
    print(f"[Mock Server] Sent: {server_drone_config_response}")


def server_set_up_spawn_drone(socket):
    server_expected_drone_request = 's:Maurice;b4788e8f'
    message = socket.recv().decode("UTF-8")
    if message != server_expected_drone_request:
        raise Exception(f'Request drone message from visualization should be "{server_expected_drone_request}" '
                        f'but was "{message}"')
    print(f"[Mock Server] Recv: {message}")

    server_drone_request_response = "1,10000,11000"
    socket.send(server_drone_request_response.encode("UTF-8"))
    print(f"[Mock Server] Sent: {server_drone_request_response}")


def server_respond_to_heartbeat(socket):
    server_expected_heartbeat = 'beep'
    try:
        message = socket.recv(flags=zmq.NOBLOCK).decode("UTF-8")
    except zmq.Again:
        return
    if message != server_expected_heartbeat:
        raise Exception(f'Heartbeat message from visualization should be "{server_expected_heartbeat}" '
                        f'but was "{message}"')
    print(f"[Mock Server] Recv: {message}")

    server_heartbeat_response = "ok"
    socket.send(server_heartbeat_response.encode("UTF-8"))
    print(f"[Mock Server] Sent: {server_heartbeat_response}")


def server_send_drone_status(socket, server_drone_status):
    socket.send(server_drone_status.encode("UTF-8"))
    print(f"[Mock Server] Sent: {server_drone_status}")


def server_respond_to_joystick(socket):
    try:
        message = socket.recv(flags=zmq.NOBLOCK).decode("UTF-8")
    except zmq.Again:
        return
    print(f"[Mock Server] Recv: {message}")

    server_joystick_response = "ok"
    socket.send(server_joystick_response.encode("UTF-8"))
    print(f"[Mock Server] Sent: {server_joystick_response}")
