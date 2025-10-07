export type Fact = { stat?: string; body: string; source?: string };

export const FACTS: Fact[] = [
  // Plastics
  { stat: '5 trillion', body: 'There are over 5 trillion pieces of plastic debris in the ocean.', source: 'National Geographic' },
  { body: 'A single plastic bag can take 1,000 years to degrade.', source: 'Center for Biological Diversity' },
  { body: 'By 2050, there could be more plastic than fish in the ocean by weight.', source: 'Ellen MacArthur Foundation' },
  { body: 'Most plastic can only be recycled once or twice before its quality degrades.', source: 'NPR' },
  { body: 'Microplastics have been found in drinking water, salt, and even the air we breathe.', source: 'WHO' },
  { body: 'The plastic industry accounts for about 6% of global oil consumption.', source: 'UNEP' },
  { body: 'Only plastics #1 (PETE) and #2 (HDPE) are widely recyclable in most municipal systems.', source: 'Plastics for Change' },
  { body: 'Plastic production has increased exponentially, from 2.3 million tons in 1950 to 448 million tons by 2015.', source: 'Our World in Data' },
  { body: 'Styrofoam (polystyrene) is not biodegradable and is very difficult to recycle.', source: 'Chemical & Engineering News' },
  { body: 'Bubble wrap is recyclable, but must be taken to special drop-off locations with other plastic films.', source: 'Earth911' },

  // Paper & Cardboard
  { body: 'It takes about 2-6 weeks for a paper towel to decompose in a landfill.', source: 'Science Focus' },
  { stat: '40%', body: 'Paper accounts for about 40% of all waste in the United States.', source: 'University of Southern Indiana' },
  { body: 'Recycling one ton of cardboard saves 46 gallons of oil.', source: 'Recycle Across America' },
  { body: 'Greasy pizza boxes are often not recyclable because the oil contaminates the paper fibers.', source: 'Stanford University' },
  { body: 'Making paper from recycled content requires 50% less water than making it from raw materials.', source: 'EPA' },
  { body: 'Deforestation is a major issue; about 42% of all global wood harvest is used to make paper.', source: 'The World Counts' },
  { body: 'The average office worker uses about 10,000 sheets of copy paper each year.', source: 'The Paperless Project' },
  { body: 'Paper and cardboard make up the largest component of municipal solid waste.', source: 'EPA' },
  { body: 'Glossy magazines and wrapping paper are often not recyclable due to their coatings and dyes.', source: 'Recycle Now' },
  { stat: '68 million', body: 'Over 68 million trees are cut down each year to produce paper and paper products.', source: 'The World Counts' },

  // Glass
  { body: 'Colored glass bottles are separated during recycling because different colors have different chemical compositions.', source: 'Glass Packaging Institute' },
  { stat: '1 million', body: 'A glass bottle can take up to 1 million years to break down in a landfill.', source: 'GPAC' },
  { body: 'Using crushed recycled glass (cullet) in manufacturing reduces air pollution by 20%.', source: 'Strategic Materials' },
  { body: 'Recycling glass is 33% more energy-efficient than creating new glass from raw materials.', source: 'EPA' },
  { body: 'Window panes and Pyrex are not recyclable in curbside bins because they melt at different temperatures.', source: 'Earth911' },
  { body: 'The U.S. throws away enough glass every two weeks to fill the Empire State Building.', source: 'Recycle Across America' },
  { body: 'All new glass containers contain at least some recycled glass.', source: 'Glass Packaging Institute' },
  { body: 'Glass is heavier than plastic, making its transportation more carbon-intensive.', source: 'Stanford University' },
  { body: 'Clear glass is the most valuable for recycling as it can be made into any new color.', source: 'O-I Glass' },
  { body: 'Recycling just one glass jar saves enough energy to light a CFL bulb for 20 hours.', source: 'Keep America Beautiful' },

  // Metals
  { body: 'Aluminum can be recycled indefinitely using only 5% of the energy needed to create it new.', source: 'The Aluminum Association' },
  { stat: '75%', body: 'Nearly 75% of all aluminum ever produced is still in use today thanks to recycling.', source: 'The Aluminum Association' },
  { body: 'Steel is the most recycled material on the planet.', source: 'World Steel Association' },
  { body: 'Recycling steel saves 74% of the energy used to produce it from raw materials.', source: 'American Iron and Steel Institute' },
  { body: 'A used aluminum can is recycled and back on the grocery shelf as a new can in as little as 60 days.', source: 'Ball Corporation' },
  { body: 'Tossing away one aluminum can is like pouring out 6 ounces of gasoline.', source: 'Recycle Across America' },
  { body: 'Most "tin cans" are actually 99% steel.', source: 'CMI' },
  { body: 'Americans throw away enough aluminum every 3 months to rebuild our entire commercial air fleet.', source: 'Environmental Defense Fund' },
  { body: 'Recycling aluminum cans creates 95% less air pollution than making them from raw materials.', source: 'EPA' },
  { stat: '21x', body: 'Recycling aluminum is 21 times more energy-efficient than making it from bauxite ore.', source: 'The World Counts' },

  // E-Waste
  { stat: '$62.5B', body: 'The raw materials in e-waste are estimated to be worth over $62.5 billion annually.', source: 'WEF' },
  { body: 'For every 1 million cell phones recycled, 35,000 lbs of copper can be recovered.', source: 'EPA' },
  { body: 'A single computer can contain toxic materials like lead, cadmium, and mercury.', source: 'WHO' },
  { body: 'Only 17.4% of global e-waste was documented as being collected and properly recycled in 2019.', source: 'Global E-waste Monitor' },
  { body: 'E-waste contains precious metals like gold and silver; there is 100 times more gold in a ton of e-waste than in a ton of gold ore.', source: 'UNEP' },
  { body: 'Improper e-waste disposal is a major source of toxic pollution in developing countries.', source: 'WHO' },
  { stat: '80%', body: 'About 80% of e-waste in the U.S. is disposed of in landfills or incinerators.', source: 'Comptia' },
  { body: 'It takes 1.5 tons of water, 48 lbs of chemicals, and 530 lbs of fossil fuels to manufacture one computer and monitor.', source: 'Electronics TakeBack Coalition' },
  { body: 'The average lifespan of a new computer has shrunk from 6 years in 1997 to just 2 years today.', source: 'Electronics TakeBack Coalition' },
  { body: 'Recycling one million laptops saves the energy equivalent to the electricity used by 3,657 U.S. homes in a year.', source: 'EPA' },
  
  // Food Waste
  { stat: '1/3', body: 'Roughly one-third of the food produced in the world for human consumption every year gets lost or wasted.', source: 'FAO' },
  { body: 'If food waste were a country, it would be the third-largest emitter of greenhouse gases after the USA and China.', source: 'FAO' },
  { body: 'Food waste consumes about 25% of all freshwater used by agriculture.', source: 'RTS' },
  { body: 'An average American family of four throws out $1,600 a year in produce.', source: 'USDA' },
  { body: 'Cutting food waste is one of the most effective single actions to combat climate change.', source: 'Project Drawdown' },
  { body: 'The water used to grow food that is wasted could be used by 9 billion people at 200 liters per person per day.', source: 'The World Counts' },
  { body: '28% of the world’s agricultural land is used to produce food that is ultimately lost or wasted.', source: 'FAO' },
  { body: 'Composting food scraps keeps them out of landfills, where they produce methane, a potent greenhouse gas.', source: 'UNEP' },
  { body: 'In developed countries, more than 40% of food waste occurs at the retail and consumer levels.', source: 'FAO' },
  { body: '“Ugly” fruits and vegetables are often thrown away by retailers, contributing significantly to food waste.', source: 'National Geographic' },

  // Textiles & Other Waste
  { stat: '15 years', body: 'The average person wears a piece of clothing for half as long as they did 15 years ago.', source: 'Ellen MacArthur Foundation' },
  { body: 'It takes over 2,700 liters of water to produce one cotton t-shirt—enough for one person to drink for 900 days.', source: 'WWF' },
  { stat: '85%', body: 'About 85% of all textiles thrown away in the U.S. are dumped into landfills or burned.', source: 'EPA' },
  { body: 'Synthetic fibers like polyester can take hundreds of years to decompose.', source: 'Fashion Revolution' },
  { body: 'A single car tire can take 50-80 years to decompose.', source: 'Business Insider' },
  { body: 'Disposable diapers take approximately 450 years to decompose in landfills.', source: 'EPA' },
  { stat: '94%', body: 'Nearly 94% of U.S. residents now have access to some type of recycling program.', source: 'Sustainable Packaging Coalition' },
  { body: 'Recycling and reuse activities in the U.S. account for 757,000 jobs.', source: 'EPA' },
  { body: 'The global waste industry is valued at over $410 billion.', source: 'Statista' },
  { body: 'A simple reusable water bottle can save an average of 156 plastic bottles annually.', source: 'Earth Day Network' },
  
  // General & Landfill Facts
  { body: 'The first government-sponsored recycling program in the U.S. was established in 1970 in Washington State.', source: 'History Channel' },
  { body: 'Modern landfills are designed to contain toxins, but leaks can still contaminate groundwater.', source: 'National Geographic' },
  { stat: '25%', body: 'Landfills are the third-largest source of human-related methane emissions in the United States.', source: 'EPA' },
  { body: 'The Great Pacific Garbage Patch is the largest accumulation of ocean plastic, twice the size of Texas.', source: 'The Ocean Cleanup' },
  { body: 'Austria and Germany recycle the most municipal waste in the world, at over 60%.', source: 'Eurostat' },
  { body: 'The concept of "zero waste" aims to eliminate trash sent to landfills, incinerators, or the ocean.', source: 'Zero Waste International Alliance' },
  { body: '"Downcycling" is when a material is recycled into a lower-quality product (e.g., plastic bottles into carpet).', source: 'Cradle to Cradle' },
  { body: 'The three arrows of the recycling symbol represent collection, manufacturing, and purchasing recycled products.', source: 'Gizmodo' },
  { stat: '2.01 billion', body: 'The world generates 2.01 billion tonnes of municipal solid waste annually.', source: 'World Bank' },
  { body: 'By 2050, global waste is expected to grow to 3.40 billion tonnes.', source: 'World Bank' },
  { body: 'A staggering 99% of the stuff we buy is trashed within 6 months.', source: 'The Story of Stuff' },
  { body: 'Cigarette butts are the most common type of plastic waste found in the environment.', source: 'NBC News' },
  { body: 'Most wrapping paper is not recyclable due to its thinness, dyes, and plastic coatings.', source: 'BBC' },
  { body: 'Used motor oil can be re-refined and used again, reducing the need for virgin crude oil.', source: 'American Petroleum Institute' },
  { body: 'Household hazardous waste, like paint and pesticides, should never be put in regular trash.', source: 'EPA' },
  { body: 'Bringing reusable bags to the grocery store can save hundreds of plastic bags per person each year.', source: 'National Geographic' },
  { body: 'Recycling one ton of mixed paper saves the energy equivalent of 185 gallons of gasoline.', source: 'EPA' },
  { stat: '6 months', body: 'An orange peel can take up to 6 months to decompose.', source: 'National Park Service' },
  { body: 'The term "wishcycling" describes putting non-recyclable items in the recycling bin, hoping they get recycled.', source: 'NPR' },
  { body: 'Buying products made from recycled materials closes the recycling loop.', source: 'EPA' },
];
```


