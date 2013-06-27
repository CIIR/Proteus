while read path; do
  scp swarm:$path abbyy/
done < portland40.list
