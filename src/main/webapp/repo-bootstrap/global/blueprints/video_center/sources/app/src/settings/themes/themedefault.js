const theme = {};

theme.palette = {
  primary: [
    '#141519',  //0: Dark Gray (background)
    '#23252a',  //1: Light Gray (menus, footer)
    '#1c1f24',  //2: Alternate Gray (cards)
    '#db0a40',  //3: Highlight Color
    '#ffffff'   //4: Highlight Contrast
  ],

  color: [
    '#ffffff',  //0: Default
    '#141519',  //1: Default Contrast
    'hsla(0.0%, 100%, .5)',  //2: Non active text
    'hsla(0,0%,100%,.7)'     //3: Non active menu text
  ]
};

export default theme;
