
cd src/main/generated/edu/umass/ciir/proteus/protocol/
sed -e '/com.google.protobuf.GeneratedMessage.BuilderParent/!s/BuilderParent/com.google.protobuf.GeneratedMessage.BuilderParent/g' ProteusProtocol.java > pp.java
mv pp.java ProteusProtocol.java


