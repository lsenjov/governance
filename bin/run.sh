
clj -A:dev \
  -i src/clj/governance/core.clj \
  -m shadow.cljs.devtools.cli \
  watch main
