import math

from uav_state_generator import IUAVStateGenerator


class UAVStateGenerator(IUAVStateGenerator):
    def __init__(self):
        self.id = 1
        self.val = 0
        self.increment = 10

        self.last_x = 0
        self.last_y = 0
        self.last_z = 0
        self.last_t = -1

    def getPosOriVel(self, time):
        x = 0
        y = 0
        z = 0
        q0 = 1
        qx = 0
        qy = 0
        qz = 0

        t = time % 8.0
        if (t < 2.0):
            val = t / 2.0
        elif (t < 6.0):
            val = 2 - t / 2.0
        else:
            val = -4 + t / 2.0

        mode = math.floor(time / 8) % 6

        angle = math.pi / 4 * val

        if (mode == 0):
            x = 5 * val
        if (mode == 1):
            y = 5 * val
        if (mode == 2):
            z = 5 * val
        if (mode == 3):
            q0 = math.cos(angle)
            qx = math.sin(angle)
        if (mode == 4):
            q0 = math.cos(angle)
            qy = math.sin(angle)
        if (mode == 5):
            q0 = math.cos(angle)
            qz = math.sin(angle)

        vx = x - (self.last_x) / (time - self.last_t)
        vy = y - (self.last_y) / (time - self.last_t)
        vz = z - (self.last_z) / (time - self.last_t)

        self.last_x = x
        self.last_y = y
        self.last_z = z
        self.last_t = time

        return x, y, z, q0, qx, qy, qz, vx, vy, vz

    def getState(self, time):
        x, y, z, q0, qx, qy, qz, vx, vy, vz = self.getPosOriVel(time)

        fi, theta, psi = 0.0, 0.0, 0.0

        self.val = self.val + self.increment

        om1, om2, om3, om4 = self.val, self.val, self.val, self.val

        state_string = (f"{self.id},{time:.2f},{x},{y},{z},{q0},{qx},{qy},{qz},{vx},{vy},{vz},{fi},{theta},{psi},{om1},{om2},{om3},{om4};"
                        f"{self.id+1},{time:.2f},{y},{x+1},{z},{q0},{-qx},{-qy},{-qz},{vx},{vy},{vz},{fi},{theta},{psi},{om1},{om2},{om3},{om4}")

        if self.val == 1000:
            self.increment = -self.increment

        elif self.val == 0:
            self.increment = -self.increment

        return state_string