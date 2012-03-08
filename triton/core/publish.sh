sbt update protobuf:generate compile
sh fixProtobuf.sh
sbt publish-local

