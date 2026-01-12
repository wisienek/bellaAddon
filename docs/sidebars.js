/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  mainSidebar: [
    'index',
    {
      type: 'category',
      label: 'Getting Started',
      collapsible: false,
      items: [
        'getting-started/requirements',
        'getting-started/installation',
        'getting-started/building',
      ],
    },
    {
      type: 'category',
      label: 'Configuration',
      items: [
        'configuration/config-files',
        'configuration/config-yml',
        'configuration/database-yml',
        'configuration/tp-configs',
      ],
    },
    {
      type: 'category',
      label: 'Commands',
      items: ['commands/overview'],
    },
    {
      type: 'category',
      label: 'Features',
      items: [
        'features/backpack',
        'features/teleportation',
        'features/money',
        'features/discord-bot',
        'features/item-enchanting',
      ],
    },
    {
      type: 'category',
      label: 'Events',
      items: [
        'events/custom-events',
        'events/listened-events',
      ],
    },
    {
      type: 'category',
      label: 'API',
      items: ['api/overview'],
    },
    {
      type: 'category',
      label: 'Development',
      items: [
        'development/project-structure',
        'development/contributing',
        'development/extending',
        'development/testing',
      ],
    },
    {
      type: 'link',
      label: 'JavaDoc',
      href: 'javadoc/index.html',
    },
  ],
};

module.exports = sidebars;
