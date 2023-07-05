#pragma version(1)
#pragma rs java_package_name(io.github.pknujsp)
#pragma rs_fp_relaxed


static uint32_t width_default = 256; //6
uint32_t width; //4

static uint32_t get_width_half(){ //7
    return width/2;
}
void change_width(uint32_t new_width){ //5
    width = new_width;
}

uchar4 RS_KERNEL invert(uchar4 in, uint32_t x, uint32_t y) { //9
  uchar4 out = in;

  if(x > get_width_half()){
    out.r = 255 - in.r;
    out.g = 255 - in.g;
    out.b = 255 - in.b;
  }
  return out;
}

void prepare(){ //8
    width = width_default;
}
