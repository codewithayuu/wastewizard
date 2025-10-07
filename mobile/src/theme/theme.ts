import { DefaultTheme, DarkTheme, Theme } from '@react-navigation/native';

export const LightTheme: Theme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: '#F6F8FA',
    card: '#FFFFFF',
    text: '#111827',
    border: '#E5E7EB',
    primary: '#10B981',
    notification: '#10B981',
  },
};

export const DarkTheme_: Theme = {
  ...DarkTheme,
  colors: {
    ...DarkTheme.colors,
    background: '#0B0F14',
    card: '#0E1116',
    text: '#E5E7EB',
    border: '#1F2937',
    primary: '#10B981',
    notification: '#10B981',
  },
};

