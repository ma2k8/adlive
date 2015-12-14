-- -----------------------------------------------------
-- Table `adlive`.`channel`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `adlive_db`.`channel` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` TIMESTAMP NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `adlive`.`channel_url`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `adlive_db`.`channel_url` (
  `channel_id` INT UNSIGNED NOT NULL,
  `url` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` TIMESTAMP NULL,
  PRIMARY KEY (`channel_id`))
ENGINE = InnoDB
COMMENT = '配信URL';


-- -----------------------------------------------------
-- Table `adlive`.`stream_url`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `adlive_db`.`stream_url` (
  `channel_id` INT UNSIGNED NOT NULL,
  `url` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` TIMESTAMP NULL,
  INDEX `fk_channel_id_idx` (`channel_id` ASC),
  PRIMARY KEY (`channel_id`))
ENGINE = InnoDB
COMMENT = 'ライブ動画を受け取るURL';

