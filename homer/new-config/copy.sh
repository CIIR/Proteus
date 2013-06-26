while read path; do
  scp swarm:$path linked/
done < portland40.list
