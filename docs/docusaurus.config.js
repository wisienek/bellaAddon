// @ts-check

const config = {
  title: 'BellaAddon',
  tagline: 'Helper plugin for BelorisRP (Spigot/Magma 1.20.1)',
  url: 'https://wisienek.github.io',
  baseUrl: '/bellaAddon/',
  organizationName: 'wisienek',
  projectName: 'bellaAddon',
  deploymentBranch: 'gh-pages',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.svg',
  trailingSlash: false,

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          path: 'docs',
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/wisienek/bellaAddon/edit/master/docs/',
          showLastUpdateAuthor: true,
          showLastUpdateTime: true,
        },
        blog: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],

  themes: ['@easyops-cn/docusaurus-search-local'],

  themeConfig: {
    navbar: {
      title: 'BellaAddon',
      items: [
        { type: 'docSidebar', sidebarId: 'mainSidebar', position: 'left', label: 'Docs' },
        { href: 'https://github.com/wisienek/bellaAddon', label: 'GitHub', position: 'right' },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            { label: 'Getting Started', to: '/getting-started/requirements' },
            { label: 'Configuration', to: '/configuration/config-files' },
            { label: 'Commands', to: '/commands/overview' },
            { label: 'API', to: '/api/overview' },
          ],
        },
        {
          title: 'Community',
          items: [
            { label: 'GitHub Issues', href: 'https://github.com/wisienek/bellaAddon/issues' },
          ],
        },
        {
          title: 'More',
          items: [
            { label: 'GitHub', href: 'https://github.com/wisienek/bellaAddon' },
          ],
        },
      ],
      copyright: `© ${new Date().getFullYear()} BellaAddon.`,
    },
    prism: {
      theme: require('prism-react-renderer').themes.github,
      darkTheme: require('prism-react-renderer').themes.dracula,
    },
    colorMode: {
      defaultMode: 'light',
      respectPrefersColorScheme: true,
    },
    search: {
      hashed: true,
    },
  },
};

module.exports = config;
