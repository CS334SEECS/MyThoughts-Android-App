package org.fog.entities.microservicesBased;

import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.placement.microservicesBased.MicroservicePlacementLogic;
import org.fog.placement.microservicesBased.PlacementLogicOutput;
import org.fog.utils.Config;
import org.fog.utils.FogUtils;
import org.fog.utils.ModuleLaunchConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samodha Pallewatta on 8/29/2019.
 */
public class ControllerComponent {

    protected LoadBalancer loadBalancer;
    protected MicroservicePlacementLogic microservicePlacementLogic = null;
    protected ServiceDiscoveryInfo serviceDiscoveryInfo = new ServiceDiscoveryInfo();

    protected int deviceId;

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }


    // Resource Availability Info
    /**
     * Resource Identifiers
     */
    public static final String RAM = "ram";
    public static final String CPU = "cpu";
    public static final String STORAGE = "storage";

    /**
     * DeviceID,<ResourceIdentifier,AvailableResourceAmount>
     */
    protected Map<Integer, Map<String, Double>> resourceAvailability = new HashMap<>();


    //Application Info
    private Map<String, Application> applicationInfo = new HashMap<>();

    //FOg Architecture Info
    private List<FogDevice> fogDeviceList;


    /**
     * For FON
     *
     * @param loadBalancer
     * @param mPlacement
     */
    public ControllerComponent(Integer deviceId, LoadBalancer loadBalancer, MicroservicePlacementLogic mPlacement,
                               Map<Integer, Map<String, Double>> resourceAvailability, Map<String, Application> applicationInfo, List<FogDevice> fogDevices) {
        this.fogDeviceList = fogDevices;
        this.loadBalancer = loadBalancer;
        this.applicationInfo = applicationInfo;
        this.microservicePlacementLogic = mPlacement;
        this.resourceAvailability = resourceAvailability;
        setDeviceId(deviceId);
    }

    /**
     * For FCN
     *
     * @param loadBalancer
     */
    public ControllerComponent(Integer deviceId, LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        setDeviceId(deviceId);
    }

    /**
     * 1. execute placement logic -> returns the placement mapping.
     * 2. deploy on devices.
     * 3. update service discovery.
     */
    public PlacementLogicOutput executeApplicationPlacementLogic(List<PlacementRequest> placementRequests) {
        if (microservicePlacementLogic != null) {
            PlacementLogicOutput placement = microservicePlacementLogic.run(fogDeviceList, applicationInfo, resourceAvailability, placementRequests);
            return placement;
        }

        return null;
    }

    public void addServiceDiscoveryInfo(String microserviceName, Integer deviceID) {
        this.serviceDiscoveryInfo.addServiceDIscoveryInfo(microserviceName, deviceID);
    }

    public int getDestinationDeviceId(String destModuleName) {
        return loadBalancer.getDeviceId(destModuleName, serviceDiscoveryInfo);
    }

    public Application getApplicationPerId(String appID) {
        return applicationInfo.get(appID);
    }

    public Double getAvailableResource(int deviceID, String resourceIdentifier) {
        if (resourceAvailability.containsKey(deviceID))
            return resourceAvailability.get(deviceID).get(resourceIdentifier);
        else
            return null;
    }

    public void updateResources(int device, String resourceIdentifier, double remainingResourceAmount) {
        if (resourceAvailability.containsKey(device))
            resourceAvailability.get(device).put(resourceIdentifier, remainingResourceAmount);
        else {
            Map<String, Double> resources = new HashMap<>();
            resources.put(resourceIdentifier, remainingResourceAmount);
            resourceAvailability.put(device, resources);
        }
    }

    public void updateResourceInfo(int deviceId, Map<String, Double> resources) {
        resourceAvailability.put(deviceId,resources);
    }
}

class ServiceDiscoveryInfo {
    protected Map<String, List<Integer>> serviceDiscoveryInfo = new HashMap<>();

    public void addServiceDIscoveryInfo(String microservice, Integer device) {
        if (serviceDiscoveryInfo.containsKey(microservice)) {
            List<Integer> deviceList = serviceDiscoveryInfo.get(microservice);
            deviceList.add(device);
            serviceDiscoveryInfo.put(microservice, deviceList);
        } else {
            List<Integer> deviceList = new ArrayList<>();
            deviceList.add(device);
            serviceDiscoveryInfo.put(microservice, deviceList);
        }
    }

    public Map<String, List<Integer>> getServiceDiscoveryInfo() {
        return serviceDiscoveryInfo;
    }
}









