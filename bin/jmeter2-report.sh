#!/usr/bin/env sh
# JMeter2 — non-GUI run + HTML dashboard report
# Usage:
#   ./jmeter2-report.sh plan.jmx
#   ./jmeter2-report.sh plan.jmx results/out

set -eu
BIN=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PLAN=${1:-}
OUTDIR=${2:-}

if [ -z "$PLAN" ]; then
  echo "Usage: $0 <testplan.jmx> [output-dir]"
  exit 2
fi
if [ ! -f "$PLAN" ]; then
  echo "Test plan not found: $PLAN"
  exit 3
fi

if [ -z "$OUTDIR" ]; then
  OUTDIR="$BIN/../reports/$(basename "$PLAN" .jmx)-$(date +%Y%m%d-%H%M%S)"
fi

mkdir -p "$OUTDIR"
JTL="$OUTDIR/results.jtl"
HTML="$OUTDIR/html"

echo "Running non-GUI test..."
"$BIN/jmeter2.sh" -n -t "$PLAN" -l "$JTL" -e -o "$HTML"

echo ""
echo "JTL : $JTL"
echo "HTML: $HTML/index.html"
