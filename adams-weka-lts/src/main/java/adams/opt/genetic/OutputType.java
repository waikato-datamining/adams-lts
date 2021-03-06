/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * OutputType.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.opt.genetic;

/**
 * Defines what to output during a genetic algorithm run.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public enum OutputType {
  /** no output. */
  NONE,
  /** only the setup. */
  SETUP,
  /** only the data. */
  DATA,
  /** setup and data. */
  ALL
}
