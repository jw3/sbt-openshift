# SBT OpenShift #

POC extension of the great SBT Native Packager with support for building OpenShift compatible images.

## How does it work ##

Currently leverages the capability of Packager's Docker Plugin.  The OpenShift Plugin mostly just reconfigures the existing build process as defined in Packager.

## What does OpenShift compatible mean? ##

See the [guidelines](https://docs.openshift.org/latest/creating_images/guidelines.html) for a full description.

### Support Arbitrary User IDs ###
    
By default, OpenShift Origin runs containers using an arbitrarily assigned user ID. This provides additional security against processes escaping the container due to a container engine vulnerability and thereby achieving escalated permissions on the host node.

For an image to support running as an arbitrary user, directories and files that may be written to by processes in the image should be owned by the root group and be read/writable by that group. Files to be executed should also have group execute permissions.

Adding the following to your Dockerfile sets the directory and file permissions to allow users in the root group to access them in the built image:

```bash
RUN chgrp -R 0 /some/directory \
  && chmod -R g+rwX /some/directory
```

## Example ##

The following is generated the default configuration other than exposing `9000` with `dockerExposedPorts := Seq(9000)`

```dockerfile
FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift
USER root
WORKDIR /opt/docker
ADD opt /opt
RUN chgrp -R 0 . \
 && chmod -R g+rwX .
EXPOSE 9000
USER 10001
CMD ["bin/example"]
```

## Developers ##
