(ns governance.components.raw
  (:require ["@blueprintjs/core" :as bjs-core]
            ["@blueprintjs/datetime" :as bjs-date]
            [oops.core :refer [oget oget+]]
            [reagent.core :as r])
  (:require-macros [governance.components.raw.macros :refer [cdefs]]))

(cdefs bjs-core
       [AnchorButton,
        Boundary,
        Breadcrumbs,
        Button,
        Card,
        Checkbox,
        H5,
        InputGroup,
        Navbar, NavbarDivider, NavbarGroup, NavbarHeading,
        Label,
        RadioGroup,
        Slider,
        Text])

(cdefs bjs-date
       [DateRangePicker])