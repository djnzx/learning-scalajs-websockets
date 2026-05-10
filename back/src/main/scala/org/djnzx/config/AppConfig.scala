package org.djnzx.config

import pureconfig.ConfigReader

case class AppConfig(
  ember: EmberConfig,
) derives ConfigReader
