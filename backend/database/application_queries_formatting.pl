# USAGE: perl application_queries_formatting.pl <SQL-FILE> <DB-PROP-FILE>
#
# format of the SQL query file should be:
#   | -- comment_that_will_end_up_in_output_using_#
#   | -- $query_name
#   | ...
#   |  query
#   | ... ;
#
# output for every query will be:
# query_name=query-on-one-line
#
# Note 1: every query should end with a ';'
# Note 2: comments in queries (between '/*' and '*/') are allowed,
#   but make sure that there are no constructions like: "... */ ... /* ..."
#   on the same line.
#   However, constructions like: "... /* ... */ ..." will be handled correctly

# Check for correct usage
if (@ARGV != 2) {
  print "USAGE: perl application_queries_formatting.pl <SQL-FILE> <DB-PROP-FILE>\n\n";
  print "Format of the SQL query file should be:\n  | -- comment_that_will_end_up_in_output_using_#\n  | -- \$query_name\n  | ...\n  |  query\n  | ... ;\n\noutput for every query will be:\nquery_name=query-on-one-line\n\nNote 1: every query should end with a ';'\nNote 2: comments in queries (between '/*' and '*/') are allowed,\n    but make sure that there are no constructions like: \"... */ ... /* ...\"\n    on the same line.\n    However, constructions like: \"... /* ... */ ...\" will be handled correctly";
  exit 1;
}

# Start by copying the upper comment in the database.properties file
open(my $database_properties, "<", $ARGV[1]) || die "Can't open database properties file '$ARGV[1]'";
while (<$database_properties>) {
  if (/^#(.*)$/) {
    print "#$1\n";
  } else {
    last;
  }
}
print "\n";

my $is_in_comment = 0;
my $query = "";

# Map the formatted file to a one-query-per-line format
open (my $sql_file, "<", $ARGV[0]) || die "Can't open sql file file '$ARGV[1]'";
while (<$sql_file>) {
  chomp; # remove trailing \n
  $_ =~ s/^\s+//; # remove leading whitespaces
  $_ =~ s/\s+$//; # remove trailing whitespaces

  # ... /* ... -> $is_in_comment = 1
  if (/^.*\/\*.*$/) {
    $is_in_comment = 1;
  }

  # ... */ ... -> $is_in_comment = 0
  if (/^.*\*\/.*$/) {
    $is_in_comment = 0;
  }

  if (/^$/) {
    print "\n" unless $is_in_comment == 1;
  }

  if (/^-- \$(.*)$/) {
    print "$1=";
  } elsif (/^-- (.*)$/) {
    print "# $1\n";
  } elsif (!/^\s+$/) {
    if ($query =~ /^\s*$/) {
      $query = $_;
    } else {
      $query = "$query $_";
    }

    if (/^.*;.*$/) {
      print "$query";
      $query = "";
    }
  }
}

# Skip all current queries in the database.properties
while (<$database_properties>) {
  last if (/^# columns$/);
}

# Reprint '# columns'
print "\n\n# columns\n";

# Print all columns in database.properties
while (<$database_properties>) {
  print $_;
}
