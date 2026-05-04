import socket, uuid, requests
from dataclasses import dataclass, asdict

@dataclass
class MachineInfo:
    hostname: str
    ipAddress: str
    ID: int
    macAddress: str


def get_machine_info():
    hostname = socket.gethostname()

    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    ip = s.getsockname()[0]
    s.close()

    mac = uuid.getnode()
    mac_address = ':'.join(f'{(mac >> ele) & 0xff:02x}' for ele in range(40, -1, -8))

    ID = int(mac_address.replace(":", ""), 16)

    machine = MachineInfo(
        hostname=hostname,
        ipAddress=ip,
        ID=ID,
        macAddress=mac_address
    )

    return asdict(machine)

data = get_machine_info()


# Alterar endpoint para enviar a machine info para o backend
#response = requests.post(
#    "http://daviipkp:4500/boot",
#    json=data
#)

