import math

from uav_state_generator import IUAVStateGenerator


class UAVStateGenerator(IUAVStateGenerator):
    def __init__(self):
        self.id = 1
        self.val = 0
    def getPosOriVel(self, time):
        q01 = 1
        qx1 = qy1 = qz1 = 0
        x2 = 0
        y2 = 0
        z2 = -10

        val = float(time % 10) / 10

        mode = math.floor(time / 10) % 4

        angle = 2 * math.pi * val

        if (mode == 0):
            x2 = math.cos(angle) * 5
            y2 = math.sin(angle) * 5
        if (mode == 1):
            x2 = 0
            y2 = math.sin(angle) * 5
            z2 = z2 + math.cos(angle) * 5
        if (mode == 2):
            x2 = math.sin(angle) * 5
            y2 = 0
            z2 = z2 + math.cos(angle) * 5
        if (mode == 3):
            x2 = math.cos(angle) * 5
            y2 = math.sin(angle) * 5
            q01 = math.cos(math.pi / 2)
            qx1 = math.sin(math.pi / 2)
        if (mode == 3):
            x2 = math.cos(angle) * 5
            y2 = math.sin(angle) * 5
            q01 = math.cos(math.pi)
            qx1 = math.sin(math.pi / 2)
        if (mode == 3):
            x2 = math.cos(angle) * 5
            y2 = math.sin(angle) * 5
            q01 = math.cos(math.pi / 2)
            qz1 = math.sin(math.pi / 4)


        return q01, qx1, qy1, qz1, x2, y2, z2

    def getState(self, time):
        x1 = y1 = 0
        z1 = -10
        q01, qx1, qy1, qz1, x2, y2, z2 = self.getPosOriVel(time)
        q02 = 1
        qx2 = qy2 = qz2 = 0
        vx = vy = vz = 0
        fi = theta = psi = 0

        om1 = om2 = om3 = om4 = 0

        state_string = (f"{self.id},{time:.2f},{x1},{y1},{z1},{q01},{qx1},{qy1},{qz1},{vx},{vy},{vz},{fi},{theta},{psi},{om1},{om2},{om3},{om4};"
                        f"{self.id+1},{time:.2f},{y2},{x2},{z2},{q02},{-qx2},{-qy2},{-qz2},{vx},{vy},{vz},{fi},{theta},{psi},{om1},{om2},{om3},{om4}")

        return state_string
