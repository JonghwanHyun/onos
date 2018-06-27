# ONOS INT Service implementation
This repository includes ONOS INT service implementation, which is demonstrated at 5th P4 Workshop 2018.

For the details of the demonstration, please see followings:
* [Abstract](https://p4.org/assets/P4WS_2018/11_INT-XDP_Abstract.pdf)
* [Demo slide](https://p4.org/assets/P4WS_2018/11_Jonghwan_Hyun_INT-XDP.pdf)
* [Demo poster](https://docs.google.com/presentation/d/1ygeJs0p1Rg5b7hiVtCqT9gOggAJMb8y9N00DNfzWJeI/edit?usp=sharing) 
* [Demo video](https://youtu.be/ZXRef0IhXGM)

## Getting started
### Requirements
This work basically requires **Ubuntu 18.04 with kernel version >= v4.14**.

Following software are also required:
* Python 2.7
* python-pip
* mininet

### Build ONOS
To build ONOS, follow [this instruction](https://github.com/opennetworkinglab/onos#getting-started)

Note that you need to clone this repository, instead of ONOS repository.

### Patch onos-setup-p4-dev for Ubuntu 18.04
See [Patch for tools/dev/p4vm/install-p4-tools.sh]

### Install p4tools
```
$ onos-setup-p4-dev
```

### Install BPFCollector
See [BPFCollector](https://gitlab.com/tunv_ebpf/BPFCollector) for detailed instruction.

### Add virtual interfaces for collector
```
$ sudo ip link add veth_11 type veth peer name veth_12
$ sudo ip link add veth_21 type veth peer name veth_22
$ sudo ip link set dev veth_11 up
$ sudo ip link set dev veth_12 up
$ sudo ip link set dev veth_21 up
$ sudo ip link set dev veth_22 up
```
## How to run
- Launch ONOS
```
$ ONOS_APPS=drivers.bmv2,proxyarp,lldpprovider,hostprovider buck run onos-local -- clean
```
- Launch Mininet
```
$ sudo -E ~/onos/tools/test/topos/bmv2-demo-int.py --onos-ip=127.0.0.1 --pipeconf-id=org.onosproject.pipelines.int
```
- Launch collector
```
$ sudo systemctl start influxdb
$ sudo python -E BPFCollector/InDBClient.py veth_22
```
- Activate INT sample application
```
$ onos-app localhost activate org.onosproject.inbandtelemetry.app
```
- Add mirroring configuration for telemetry report
```
$ simple_switch_CLI --thrift-port `cat /tmp/bmv2-s12-thrift-port`
RuntimeCmd: mirroring_add 500 5
  (500: REPORT_MIRROR_SESSION_ID defined in int_definitions.p4)
  (5: port number to send mirrored packet, in this case veth_21)
```
- Add host to host intent
```
$ onos localhost
onos> add-host-intent 00:00:00:00:01:01/None 00:00:00:00:02:02/None
(Since there is no forwarding or routing application enabled, we add routes between hosts manually.)
(h11 and h22 in Mininet topology, respectively)
```

- Add collector configuration
  - Connect to ONOS web interface (http://localhost:8181/onos/ui/)
  - Open "In-band Telemetry Control" in the menu
  - Type 127.0.0.1 to "Collector IP" field and 54321 to "Collector Port" field
  - Click "Deploy" button
- Add IntIntent to specify traffic to monitor
  - Go to "In-band Telemetry Control" page
  - Fill in "Src Address", "Dst Address", "Src Port", "Dst Port" and "Protocol" field
    - Src and Dst address fields accepts IP address with mask (e.g., 10.0.0.0/24)
    - Src and Dst port number requires exact port number
    - Protocol supports either TCP or UDP
    - Any field remained empty, except Protocol field, means wildcard. 
  - Choose metadata to collect
    - **Switch Id** and **Egress timestamp** must be chosen so as to make Collecor to work properly.
    - "Queue congestion status" and "Egress Port Tx Utilization" are not supported in current version of BMv2 software switch.
  - Click "Deploy" button below the metadata to deploy INT Intent.
  - Created INT Intent will be appeared in the table below.
- Generate some traffic to monitor
```
mininet> h11 iperf -c h22 -u -t 10000 -l 250B
```
- Connect Grafana to see the collected data
  - http://localhost:3000
